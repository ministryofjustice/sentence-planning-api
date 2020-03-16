package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.application.RequestData;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.AssessmentNeed;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;
import java.util.List;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.ExpectedCount.between;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@ActiveProfiles("test,disableauthorisation")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:need/before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:need/after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class SentencePlanResource_NeedTest {


    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    OAuth2RestTemplate oauthRestTemplate;

    @Autowired
    SentencePlanRepository sentencePlanRepository;

    private static final String USER = "TEST_USER";
    private final String SENTENCE_PLAN_ID = "11111111-1111-1111-1111-111111111111";
    private final String NO_NEEDS_SENTENCE_PLAN_ID = "22222222-2222-2222-2222-222222222222";
    private final String NOT_FOUND_SENTENCE_PLAN_ID = "99999999-9999-9999-9999-999999999999";

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> mapper
        ));
        oauthRestTemplate.getOAuth2ClientContext().setAccessToken(
                new DefaultOAuth2AccessToken("accesstoken")
        );

    }

    @Test
    public void shouldGetNeedsWhenSentencePlanExists() throws JsonProcessingException {
        var assessmentApi = setupMockRestServiceServer(123456L);
        var result = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}/needs", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", NeedDto.class);

        assertThat(result).hasSize(2);
    }

    @Test
    public void shouldGetEmptyArrayWhenNoNeedsExist() throws JsonProcessingException {
        var assessmentApi = setupMockRestServiceServerNoNeeds();
        var result = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}/needs", NO_NEEDS_SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", NeedDto.class);

        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldReturnNotFoundForNonexistentPlan() {
        var result = given()
                .when()
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}/needs", NOT_FOUND_SENTENCE_PLAN_ID)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getDeveloperMessage()).contains("Sentence Plan " + NOT_FOUND_SENTENCE_PLAN_ID + " not found");
        assertThat(result.getUserMessage()).contains("Sentence Plan " + NOT_FOUND_SENTENCE_PLAN_ID + " not found");
        assertThat(result.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldSetNeedsWhenCreatingNewPlan() throws JsonProcessingException {

        var assessmentApi = setupMockRestServiceServer(123L);
        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysOffender(123L, "Gary", "Smith", "", "", "12345678", "123")), MediaType.APPLICATION_JSON));

        var sentencePlan = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .post("/offenders/{oasysOffenderId}/sentenceplans", 123L)
                .then()
                .statusCode(201)
                .extract()
                .body()
                .as(SentencePlanDto.class);

        assessmentApi.verify();
        assertThat(sentencePlan.getNeeds()).extracting("name").containsOnly("Accommodation", "Alcohol");
    }

    private MockRestServiceServer setupMockRestServiceServer(long offenderId) throws JsonProcessingException {
        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();

        var needs = List.of(new AssessmentNeed("Alcohol", true, true, true, true),
                new AssessmentNeed("Accommodation", true, true, true, true));

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/" + offenderId + "/assessments/latest?assessmentType=LAYER_3"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysAssessment(123456L, "ACTIVE", needs, true)), MediaType.APPLICATION_JSON));
        return assessmentApi;
    }

    private MockRestServiceServer setupMockRestServiceServerNoNeeds() throws JsonProcessingException {
        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/789123/assessments/latest?assessmentType=LAYER_3"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysAssessment(789123L, "ACTIVE", null, true)), MediaType.APPLICATION_JSON));
        return assessmentApi;
    }


}