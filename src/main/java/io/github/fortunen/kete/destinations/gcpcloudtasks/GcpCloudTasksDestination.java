package io.github.fortunen.kete.destinations.gcpcloudtasks;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.TimeUnit;

import com.google.cloud.tasks.v2.CloudTasksGrpc;
import com.google.cloud.tasks.v2.CreateTaskRequest;
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
import io.grpc.auth.MoreCallCredentials;
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
	private CloudTasksGrpc.CloudTasksBlockingStub stub;
	private Set<Map.Entry<String, String>> customHeadersEntrySet;
	private final ConcurrentHashMap<String, String> parentPathCache = new ConcurrentHashMap<>();

	@Override
	@SneakyThrows
	public void doInitialize() {

		ValidationUtils.requireNonNull(config, "config is required");

		queue = config.getQueue();
		timeout = config.getTimeout();
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

		var parent = "projects/" + config.getProject() + "/locations/" + config.getLocation();

		var listRequest = ListQueuesRequest.newBuilder()
			.setParent(parent)
			.setPageSize(1)
			.build();

		stub.withDeadlineAfter(timeout.toSeconds(), TimeUnit.SECONDS).listQueues(listRequest);
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

		stub.createTask(request);
	}

	@Override
	public void close() {
		ValidationUtils.tryClose(channel, "channel");
	}
}
