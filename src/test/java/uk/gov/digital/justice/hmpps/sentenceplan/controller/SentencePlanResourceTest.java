package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ErrorResponse;
import uk.gov.digital.justice.hmpps.sentenceplan.api.PlanStatus;
import uk.gov.digital.justice.hmpps.sentenceplan.api.SentencePlan;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class SentencePlanResourceTest {

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper mapper;
    
    private final String SENTENCE_PLAN_ID = "11111111-1111-1111-1111-111111111111";
    private final String NOT_FOUND_SENTENCE_PLAN_ID = "99999999-9999-9999-9999-999999999999";
    private HttpHeaders headers;


    @Before
    public void setup() {
        headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, APPLICATION_JSON.toString());
    }

    @Test
    public void shouldGetSentencePlanWhenExists() {
        var result = testRestTemplate.exchange(
                getBasePath() + "/sentenceplan/" + SENTENCE_PLAN_ID, GET, new HttpEntity(headers), SentencePlan.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        var plan = result.getBody();
        assertThat(plan.getUuid()).isEqualTo(UUID.fromString(SENTENCE_PLAN_ID));
        assertThat(plan.getStatus()).isEqualTo(PlanStatus.ACTIVE);
    }

    @Test
    public void shouldReturnNotFoundForNonexistentPlan() {
        var result = testRestTemplate.exchange(
                getBasePath() + "/sentenceplan/" + NOT_FOUND_SENTENCE_PLAN_ID, GET, new HttpEntity(headers), ErrorResponse.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        var response = result.getBody();
        assertThat(response.getDeveloperMessage()).contains("Sentence Plan " + NOT_FOUND_SENTENCE_PLAN_ID + " not found");
        assertThat(response.getUserMessage()).contains("Sentence Plan " + NOT_FOUND_SENTENCE_PLAN_ID + " not found");
        assertThat(response.getStatus()).isEqualTo(404);
    }

    private String getBasePath() {
        return "http://localhost:" + port;
    }
}