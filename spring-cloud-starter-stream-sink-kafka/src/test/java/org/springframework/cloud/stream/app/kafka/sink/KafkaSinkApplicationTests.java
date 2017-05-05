/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.kafka.sink;

import org.apache.kafka.clients.consumer.Consumer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.springframework.kafka.test.utils.KafkaTestUtils.getSingleRecord;
import static org.springframework.kafka.test.utils.KafkaTestUtils.producerProps;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@SpringBootTest({"server.port=-1",
		"kafka.topicExpression=payload", "kafka.messageKeyExpression=headers.key",
		"spring.kafka.consumer.groupId=testGroup", "spring.kafka.consumer.autoOffsetReset=earliest"})
public class KafkaSinkApplicationTests {

	static String topic = "testTopic";

	@ClassRule
	public static KafkaEmbedded kafka = new KafkaEmbedded(1, true, topic);

	@BeforeClass
	public static void beforeClass() {
		System.setProperty("spring.kafka.bootstrapServers", kafka.getBrokersAsString());
	}

	@Autowired
	@Bindings(KafkaSinkConfiguration.class)
	private Sink sink;

	@Autowired
	private ConsumerFactory consumerFactory;

	@Test
	public void shouldSendMessageViaSink() {
		Map<String, Object> headers = new HashMap<>();
		headers.put("key", "myKey");
		GenericMessage<String> message = new GenericMessage<>(topic, headers);
		this.sink.input().send(message);
		Consumer consumer = consumerFactory.createConsumer();
		consumer.subscribe(singletonList(topic));
		getSingleRecord(consumer, topic);
	}

	@Bean
	public ProducerFactory<?, ?> kafkaProducerFactory() {
		return new DefaultKafkaProducerFactory<Object, Object>(producerProps(kafka));
	}

	@SpringBootApplication
	static class KafkaSinkApplication {

		public static void main(String[] args) {
			SpringApplication.run(KafkaSinkApplication.class, args);
		}

	}

}
