package com.playgroundtest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.playgroundtest.PlaygroundTestApplication;
import com.playgroundtest.annotation.IntegrationTest;
import com.playgroundtest.dao.Configuration;
import com.playgroundtest.dao.ConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//https://keyholesoftware.com/2018/02/12/disabling-filtering-tests-junit-5/ Filtrar no clean install
@IntegrationTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:application-teste.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = PlaygroundTestApplication.class)
public class PlaygroundAPIIntegrationTest {

    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final Gson GSON = new GsonBuilder().setDateFormat(ISO_DATE_FORMAT).create();

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ConfigurationRepository repository;

    @BeforeEach
    public void beforeClass() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setPlaygroundUser("teste3");
        repository.save(configuration);
    }

    @Test
    public void getConfigurations() throws Exception {
        mvc.perform(get("/api/configurations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect((ResultMatcher) jsonPath("$[0].playgroundUser", is("teste3")));

    }

    @Test
    public void getConfigurationById() {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("id", "1");

        String result = restTemplate.exchange("http://localhost:7792/api/configuration/{id}", HttpMethod.GET, new HttpEntity(getHeaders()), String.class, urlParams).getBody();
        Configuration configuration = GSON.fromJson(result, Configuration.class);

        assertEquals("teste3", configuration.getPlaygroundUser());

    }

    @Test
    public void saveConfiguration() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setPlaygroundUser("Integration Test");

        String json = mapper.writeValueAsString(configuration);

        mvc.perform(post("/api/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.playgroundUser", is("Integration Test")));
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("oi", "47.");
        headers.set("accept-language", "pt");
        return headers;
    }
}
