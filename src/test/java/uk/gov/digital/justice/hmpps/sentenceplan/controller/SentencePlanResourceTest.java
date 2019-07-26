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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import uk.gov.digital.justice.hmpps.sentenceplan.api.CreateSentencePlanRequest;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ErrorResponse;
import uk.gov.digital.justice.hmpps.sentenceplan.api.PlanStatus;
import uk.gov.digital.justice.hmpps.sentenceplan.api.SentencePlan;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.AssessmentNeed;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysIdentifiers;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;
import uk.gov.digital.justice.hmpps.sentenceplan.service.OffenderReferenceType;
import java.util.List;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.PlanStatus.DRAFT;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:sentencePlan/before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:sentencePlan/after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class SentencePlanResourceTest {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    OAuth2RestTemplate oauthRestTemplate;

    @Autowired
    SentencePlanRepository sentencePlanRepository;
    
    private final String SENTENCE_PLAN_ID = "11111111-1111-1111-1111-111111111111";
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
    public void shouldGetSentencePlanWhenExists() {

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplan/{0}", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(SentencePlan.class);

        assertThat(result.getUuid()).isEqualTo(UUID.fromString(SENTENCE_PLAN_ID));
        assertThat(result.getStatus()).isEqualTo(PlanStatus.STARTED);
    }

    @Test
    public void shouldReturnNotFoundForNonexistentPlan() {
        var result = given()
                .when()
                .get("/sentenceplan/{0}", NOT_FOUND_SENTENCE_PLAN_ID)
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
    public void shouldGetLatestOffenderAndLatestAssessmentForNewSentencePlan() throws JsonProcessingException {
        var assessmentApi = setupMockRestServiceServer();

        var requestBody = new CreateSentencePlanRequest("12345", OffenderReferenceType.OASYS);

        given()
            .when()
            .header("Accept", "application/json")
            .body(requestBody)
            .header("Content-Type", "application/json")
            .post("/sentenceplan")
            .then()
            .statusCode(201);

        assessmentApi.verify();
    }

    @Test
    public void shouldCreateNewDraftSentencePlan() throws JsonProcessingException {

        setupMockRestServiceServer();

        var requestBody = new CreateSentencePlanRequest("12345", OffenderReferenceType.OASYS);

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplan")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .as(SentencePlan.class);

        assertThat(result.getStatus()).isEqualTo(DRAFT);
        assertThat(result.getSteps().size()).isEqualTo(0);
        assertThat(result.getNeeds().size()).isEqualTo(2);
    }

    @Test
    public void shouldReturn400WhenCreatingPlanWithoutAnAssessment() throws JsonProcessingException {

        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/12345"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysOffender(12345L, "mr", "Gary", "Smith", "", "", new OasysIdentifiers("12345678"))), MediaType.APPLICATION_JSON));

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/12345/assessments/latest"))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        var requestBody = new CreateSentencePlanRequest("12345", OffenderReferenceType.OASYS);

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplan")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getStatus()).isEqualTo(400);

    }

    @Test
    public void shouldReturn404WhenOffenderNotFound() throws JsonProcessingException {

        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/12345"))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        var requestBody = new CreateSentencePlanRequest("12345", OffenderReferenceType.OASYS);

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplan")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getStatus()).isEqualTo(404);

    }

    private MockRestServiceServer setupMockRestServiceServer() throws JsonProcessingException {
        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();

        var needs = List.of(new AssessmentNeed("Alcohol", true, true, true, true),
                new AssessmentNeed("Accomodationßß", true, true, true, true));

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/12345"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysOffender(12345L, "mr", "Gary", "Smith", "", "", new OasysIdentifiers("12345678"))), MediaType.APPLICATION_JSON));

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/12345/assessments/latest"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysAssessment(12345L, "ACTIVE", needs, true, true)), MediaType.APPLICATION_JSON));
        return assessmentApi;
    }

}