package io.github.fortunen.kete.unittests.utils.awsutils;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fortunen.kete.utils.AwsUtils;
import org.junit.jupiter.api.Test;

public class buildSnsTopicArnTests {

	@Test
	public void shouldBuildStandardArn() {

		// arrange

		var region = "us-east-1";
		var accountId = "123456789012";
		var topic = "my-topic";

		// act

		var result = AwsUtils.buildSnsTopicArn(region, accountId, topic);

		// assert

		assertThat(result).isEqualTo("arn:aws:sns:us-east-1:123456789012:my-topic");
	}

	@Test
	public void shouldBuildArnForEuRegion() {

		// arrange

		var region = "eu-west-1";
		var accountId = "987654321098";
		var topic = "notifications";

		// act

		var result = AwsUtils.buildSnsTopicArn(region, accountId, topic);

		// assert

		assertThat(result).isEqualTo("arn:aws:sns:eu-west-1:987654321098:notifications");
	}

	@Test
	public void shouldBuildArnForChinaRegion() {

		// arrange

		var region = "cn-north-1";
		var accountId = "123456789012";
		var topic = "china-topic";

		// act

		var result = AwsUtils.buildSnsTopicArn(region, accountId, topic);

		// assert

		assertThat(result).isEqualTo("arn:aws:sns:cn-north-1:123456789012:china-topic");
	}

	@Test
	public void shouldBuildArnWithHyphenatedTopic() {

		// arrange

		var region = "ap-southeast-1";
		var accountId = "111222333444";
		var topic = "my-complex-topic-name";

		// act

		var result = AwsUtils.buildSnsTopicArn(region, accountId, topic);

		// assert

		assertThat(result).isEqualTo("arn:aws:sns:ap-southeast-1:111222333444:my-complex-topic-name");
	}
}
