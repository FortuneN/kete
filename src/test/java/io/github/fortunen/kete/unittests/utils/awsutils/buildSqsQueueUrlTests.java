package io.github.fortunen.kete.unittests.utils.awsutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.AwsUtils;
import org.junit.jupiter.api.Test;

public class buildSqsQueueUrlTests {

	// =========================================================================
	// Custom endpoint URL
	// =========================================================================

	@Test
	public void shouldBuildUrlWithCustomEndpoint() {

		// arrange

		var endpointUrl = "http://localhost:4566";
		var region = "us-east-1";
		var accountId = "123456789012";
		var queue = "my-queue";

		// act

		var result = AwsUtils.buildSqsQueueUrl(endpointUrl, region, accountId, queue);

		// assert

		assertThat(result).isEqualTo("http://localhost:4566/123456789012/my-queue");
	}

	@Test
	public void shouldBuildUrlWithCustomEndpointTrailingSlash() {

		// arrange

		var endpointUrl = "http://localhost:4566/";
		var region = "us-east-1";
		var accountId = "123456789012";
		var queue = "my-queue";

		// act

		var result = AwsUtils.buildSqsQueueUrl(endpointUrl, region, accountId, queue);

		// assert

		assertThat(result).isEqualTo("http://localhost:4566/123456789012/my-queue");
	}

	@Test
	public void shouldBuildUrlWithCustomEndpointAndPath() {

		// arrange

		var endpointUrl = "http://localstack:4566";
		var region = "us-west-2";
		var accountId = "000000000000";
		var queue = "test-queue";

		// act

		var result = AwsUtils.buildSqsQueueUrl(endpointUrl, region, accountId, queue);

		// assert

		assertThat(result).isEqualTo("http://localstack:4566/000000000000/test-queue");
	}

	// =========================================================================
	// Standard AWS regions
	// =========================================================================

	@Test
	public void shouldBuildStandardAwsUrl() {

		// arrange

		var region = "us-east-1";
		var accountId = "123456789012";
		var queue = "production-queue";

		// act

		var result = AwsUtils.buildSqsQueueUrl(null, region, accountId, queue);

		// assert

		assertThat(result).isEqualTo("https://sqs.us-east-1.amazonaws.com/123456789012/production-queue");
	}

	@Test
	public void shouldBuildStandardAwsUrlForEuRegion() {

		// arrange

		var region = "eu-west-1";
		var accountId = "987654321098";
		var queue = "events";

		// act

		var result = AwsUtils.buildSqsQueueUrl(null, region, accountId, queue);

		// assert

		assertThat(result).isEqualTo("https://sqs.eu-west-1.amazonaws.com/987654321098/events");
	}

	@Test
	public void shouldBuildStandardAwsUrlWhenEndpointIsEmpty() {

		// arrange

		var region = "ap-southeast-1";
		var accountId = "111222333444";
		var queue = "notifications";

		// act

		var result = AwsUtils.buildSqsQueueUrl("", region, accountId, queue);

		// assert

		assertThat(result).isEqualTo("https://sqs.ap-southeast-1.amazonaws.com/111222333444/notifications");
	}

	@Test
	public void shouldBuildStandardAwsUrlWhenEndpointIsBlank() {

		// arrange

		var region = "us-west-2";
		var accountId = "555666777888";
		var queue = "my-queue";

		// act

		var result = AwsUtils.buildSqsQueueUrl("   ", region, accountId, queue);

		// assert

		assertThat(result).isEqualTo("https://sqs.us-west-2.amazonaws.com/555666777888/my-queue");
	}

	// =========================================================================
	// China regions
	// =========================================================================

	@Test
	public void shouldBuildChinaRegionUrl() {

		// arrange

		var region = "cn-north-1";
		var accountId = "123456789012";
		var queue = "china-queue";

		// act

		var result = AwsUtils.buildSqsQueueUrl(null, region, accountId, queue);

		// assert

		assertThat(result).isEqualTo("https://sqs.cn-north-1.amazonaws.com.cn/123456789012/china-queue");
	}

	@Test
	public void shouldBuildChinaNorthwestRegionUrl() {

		// arrange

		var region = "cn-northwest-1";
		var accountId = "123456789012";
		var queue = "china-nw-queue";

		// act

		var result = AwsUtils.buildSqsQueueUrl(null, region, accountId, queue);

		// assert

		assertThat(result).isEqualTo("https://sqs.cn-northwest-1.amazonaws.com.cn/123456789012/china-nw-queue");
	}
}
