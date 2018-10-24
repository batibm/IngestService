/**
 * 
 */
package com.ibm.tpd.primarydb.ingestmsg.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author SangitaPal
 *
 */
//@EnableKafka
@RunWith(SpringRunner.class)
@WebMvcTest(GreetController.class)
public class GreetControllerTest{
	
	@Autowired
    private MockMvc mvc;
	
	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "testTopic");

	@BeforeClass
	public static void setup() {
		System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getBrokersAsString());
		System.setProperty("spring.kafka.bootstrap-servers", embeddedKafka.getBrokersAsString());
		System.setProperty("spring.cloud.stream.kafka.binder.zkNodes", embeddedKafka.getZookeeperConnectionString());
	}
	
	@Test
	public void greet_whenValidURLSpecified_thenInvokesAPI() throws Exception {
		
		this.mvc.perform(get("/greeting").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(content().string("hello from ingest-service !!!"));
		
	}
}
