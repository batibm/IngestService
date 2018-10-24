/**
 * 
 */
package com.ibm.tpd.primarydb.ingestmsg.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.ibm.tpd.primarydb.entity.response.SuccessResponse;
import com.ibm.tpd.primarydb.ingestmsg.service.IngestMessageService;

/**
 * @author SangitaPal
 *
 */
//@EnableKafka
@RunWith(SpringRunner.class)
@WebMvcTest(IngestMessageController.class)
public class IngestMessageControllerImplTest{
	
	private static final String TEST_RESPONSE_JSON =
			"{\"statusCode\":\"200\",\"statusDesc\":\"Message pushed to Kafka topic successfully.\"}";
	
	private static final String VALID_URL = "/tpd/primarydb/ingest?msgtype=3-1";
	private static final String INVALID_URL_MISSING_REQUEST_PARAM = "/tpd/primarydb/ingest";
	private String inputJsonMessage="";
	public static final String JSON_INPUT_FILE_NAME="applyupui-3-1.json";
	
	@MockBean
	private IngestMessageService ingestService;
	
	@Autowired
    private MockMvc mvc;
	
	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "testTopic");

	@BeforeClass
	public static void setup() {
		System.setProperty("spring.kafka.bootstrap-servers", embeddedKafka.getBrokersAsString());
		System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getBrokersAsString());
		System.setProperty("spring.cloud.stream.kafka.binder.zkNodes", embeddedKafka.getZookeeperConnectionString());
	}
	
	@Before
	public void loadInputJson() {
		ClassLoader classLoader = new IngestMessageControllerImplTest().getClass().getClassLoader();

		try {
			File file = new File(classLoader.getResource(JSON_INPUT_FILE_NAME).getFile());
			if(file.exists())
				inputJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	@Test
	public void ingestMessage_whenValidURLSpecified_thenInvokesAPI() throws Exception {
		SuccessResponse successResponse = new SuccessResponse("200", "Message pushed to Kafka topic successfully.");

		given(this.ingestService.sendMessageToKafkaTopic("3-1", inputJsonMessage)).willReturn(successResponse);

		this.mvc.perform(post(VALID_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(inputJsonMessage)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk()).andExpect(content().string(TEST_RESPONSE_JSON));

	}
	
	@Test
	public void ingestMessage_whenRequestParamNotSpecified_thenReceivesHTTPStatusCodeBadRequest() throws Exception {

		this.mvc.perform(post(INVALID_URL_MISSING_REQUEST_PARAM)
				.contentType(MediaType.APPLICATION_JSON)
				.content(inputJsonMessage)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest());

	}
}
