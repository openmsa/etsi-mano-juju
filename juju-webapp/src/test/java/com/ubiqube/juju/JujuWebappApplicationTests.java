package com.ubiqube.juju;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.yml")
@SuppressWarnings("static-method")
class JujuWebappApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Test
	void contextLoads() {
		assertTrue(true);
	}

//	@Test
	void testGetClouds() throws Exception {
		mvc.perform(get("/cloud").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isAccepted());
	}
}
