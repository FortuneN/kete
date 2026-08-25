package io.github.fortunen.kete.destinations.gcpcloudtasks;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.cloud.tasks.v2.CloudTasksGrpc;
import com.google.cloud.tasks.v2.CreateTaskRequest;
import com.google.cloud.tasks.v2.GetQueueRequest;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.cloud.tasks.v2.HttpRequest;
import com.google.cloud.tasks.v2.ListQueuesRequest;
import com.google.cloud.tasks.v2.Task;
import com.google.protobuf.ByteString;

import io.github.fortunen.kete.Component;
import io.github.fortunen.kete.Constants;
import io.github.fortunen.kete.Destination;
import io.github.fortunen.kete.EventMessage;
import io.github.fortunen.kete.utils.TemplateUtils;
import io.github.fortunen.kete.utils.ValidationUtils;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.auth.MoreCallCredentials;
import io.grpc.stub.MetadataUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Component(name = "gcp-cloud-tasks")
public class GcpCloudTasksDestination extends Destination<GcpCloudTasksDestinationConfig> {

	private String queue;
	private String targetUrl;
	private Duration timeout;
	private String httpMethod;
	private ManagedChannel channel;
	private String parentPathPrefix;
	private boolean isQueueTemplated;
	private boolean hasTimeoutSeconds;
	private CloudTasksGrpc.CloudTasksBlockingStub stub;
	private Set<Map.Entry<String, String>> customHeadersEntrySet;
	private final ConcurrentHashMap<String, String> parentPathCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		queue = config.getQueue();
		timeout = config.getTimeout();
		hasTimeoutSeconds = config.isHasTimeoutSeconds();
		targetUrl = config.getTargetUrl();
		httpMethod = config.getHttpMethod();
		isQueueTemplated = config.isQueueTemplated();
		channel = config.getChannelBuilder().build();
		stub = CloudTasksGrpc.newBlockingStub(channel);
		parentPathPrefix = config.getParentPathPrefix();
		customHeadersEntrySet = config.getCustomHeadersEntrySet();

		if (config.isAuthenticated()) {
			stub = stub.withCallCredentials(MoreCallCredentials.from(config.getCredentials()));
		}

		// verify connection

		var deadlinedStub = stub.withDeadlineAfter(timeout.toSeconds(), TimeUnit.SECONDS);

		try {

			if (isQueueTemplated) {
				var parent = "projects/" + config.getProject() + "/locations/" + config.getLocation();
				var listRequest = ListQueuesRequest.newBuilder().setParent(parent).setPageSize(1).build();
				var metadata = new Metadata();
				metadata.put(Metadata.Key.of("x-goog-request-params", Metadata.ASCII_STRING_MARSHALLER), "parent=" + parent);
				deadlinedStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata)).listQueues(listRequest);
			} else {
				var queueName = parentPathPrefix + queue;
				var getRequest = GetQueueRequest.newBuilder().setName(queueName).build();
				var metadata = new Metadata();
				metadata.put(Metadata.Key.of("x-goog-request-params", Metadata.ASCII_STRING_MARSHALLER), "name=" + queueName);
				deadlinedStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata)).getQueue(getRequest);
			}

		} catch (StatusRuntimeException exception) {

			if (exception.getStatus().getCode() != Status.Code.PERMISSION_DENIED) {
				throw exception;
			}

			// connected but no read permission, no problem
		}
	}

	@Override
	public boolean isHealthy() {
		return true; // gRPC channels re-establish transports on demand; failures surface on send
	}

	@Override
	@SneakyThrows
	public void doSend(EventMessage message) {

		ValidationUtils.requireNonNull(message, "message is required");

		// queue

		var actualQueue = isQueueTemplated ? TemplateUtils.substitute(queue, message) : queue;

		// parent path

		var parentPath = parentPathCache.computeIfAbsent(actualQueue, q -> parentPathPrefix + q);

		// headers

		var headers = new HashMap<String, String>();

		for (var entry : customHeadersEntrySet) {
			headers.put(entry.getKey(), entry.getValue());
		}

		headers.put(Constants.MESSAGE_HEADER_EVENT_KIND, message.kind());
		headers.put(Constants.MESSAGE_HEADER_EVENT_TYPE, message.eventType());
		headers.put(Constants.MESSAGE_HEADER_CONTENT_TYPE, message.contentType());

		// http request

		var payload = encodePayload(message.eventBody());

		var httpRequest = HttpRequest.newBuilder()
			.setUrl(targetUrl)
			.putAllHeaders(headers)
			.setHttpMethod(HttpMethod.valueOf(httpMethod))
			.setBody(ByteString.copyFrom(payload))
			.build();

		// task

		var task = Task.newBuilder()
			.setHttpRequest(httpRequest)
			.build();

		var request = CreateTaskRequest.newBuilder()
			.setTask(task)
			.setParent(parentPath)
			.build();

		var metadata = new Metadata();
		metadata.put(Metadata.Key.of("x-goog-request-params", Metadata.ASCII_STRING_MARSHALLER), "parent=" + parentPath);
		var callStub = hasTimeoutSeconds ? stub.withDeadlineAfter(timeout.toSeconds(), TimeUnit.SECONDS) : stub;

		callStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata)).createTask(request);
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(channel, "channel");
	}
}
