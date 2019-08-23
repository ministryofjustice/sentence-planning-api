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
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.*;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;
import uk.gov.digital.justice.hmpps.sentenceplan.service.OffenderReferenceType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static java.util.Collections.EMPTY_LIST;
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
    private final long OASYS_OFFENDER_ID = 123456;


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
    public void shouldGetSentencePlanWhenExists() throws JsonProcessingException {
        setupMockRestServiceServer();
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
    public void shouldGetSentencePlanSummaries() throws JsonProcessingException {

        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123456/properSentencePlans"))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(List.of(
                            new OasysSentencePlan(12345L, LocalDate.of(2010, 1,1), null, EMPTY_LIST)
                        )), MediaType.APPLICATION_JSON));

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/offender/{0}/sentenceplans/", OASYS_OFFENDER_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", SentencePlanSummary.class);

        assertThat(result.get(0).getPlanId()).isEqualTo(SENTENCE_PLAN_ID);
        assertThat(result.get(0).getCreatedDate()).isEqualTo(LocalDate.of(2019,6,27));

        assertThat(result.get(2).getPlanId()).isEqualTo("12345");
        assertThat(result.get(2).getCreatedDate()).isEqualTo(LocalDate.of(2010,1,1));

    }


    @Test
    public void shouldGetLegacySentencePlanIfExists() throws JsonProcessingException {

        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123456/properSentencePlans"))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(List.of(
                                new OasysSentencePlan(12345L, LocalDate.of(2010, 1,1), null, EMPTY_LIST)
                        )), MediaType.APPLICATION_JSON));

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/offender/{0}/sentenceplan/{1}", OASYS_OFFENDER_ID, 12345L)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OasysSentencePlan.class);

        assertThat(result.getOasysSetId()).isEqualTo(12345L);
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
        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();
        var needs = List.of(new AssessmentNeed("Alcohol", true, true, true, true),
                new AssessmentNeed("Accommodation", true, true, true, true));

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/12345"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysOffender(12345L, "mr", "Gary", "Smith", "", "", new OasysIdentifiers("12345678"))), MediaType.APPLICATION_JSON));

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/12345/assessments/latest"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysAssessment(123456L, "ACTIVE", needs, true, true)), MediaType.APPLICATION_JSON));

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

        var requestBody = new CreateSentencePlanRequest("123456", OffenderReferenceType.OASYS);

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
    public void shouldNotCreateNewSentencePlanIfCurrentPlanExistsForOffender() throws JsonProcessingException {

        setupMockRestServiceServer();

        var requestBody = new CreateSentencePlanRequest("123456", OffenderReferenceType.OASYS);

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

        var errorResult = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplan")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(errorResult.getStatus()).isEqualTo(400);

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

    @Test
    public void shouldUpdateMotivations() throws JsonProcessingException {


        var requestBody = List.of(
                new AssociateMotivationNeedRequest(UUID.fromString("11111111-1111-1111-1111-111111111111"), UUID.fromString("38731914-701d-4b4e-abd3-1e0a6375f0b2")));

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplan/{0}/motivations", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract().statusCode();

        assertThat(result).isEqualTo(200);

        setupMockRestServiceServer();

        var plan = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplan/{0}", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(SentencePlan.class);

        assertThat(plan.getUuid()).isEqualTo(UUID.fromString(SENTENCE_PLAN_ID));

        var needs = plan.getNeeds();
        var need = needs.stream().filter(n -> n.getMotivation().getUUID() != null).findFirst().get();
        assertThat(need.getMotivation().getUUID()).isEqualTo(UUID.fromString("38731914-701d-4b4e-abd3-1e0a6375f0b2"));

    }

    /*
    Currently the implementation uses computeIfPresent for updating motivations so an invalid uuid shouldn't cause a problem
    Only test we can do is to demonstrate that we don't get an exception.
    */
    @Test
    public void shouldUpdateMotivationsWithInvalidNeed() throws JsonProcessingException {
        setupMockRestServiceServer();

        var requestBody = List.of(
                new AssociateMotivationNeedRequest(UUID.fromString("11111111-1111-1111-1111-111111111111"), UUID.fromString("38731914-701d-4b4e-abd3-1e0a6375f0b2")),
                new AssociateMotivationNeedRequest(UUID.fromString("00000000-0000-0000-0000-000000000000"), UUID.fromString("38731914-701d-4b4e-abd3-1e0a6375f0b2"))
                );

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplan/{0}/motivations", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract().statusCode();

        assertThat(result).isEqualTo(200);

    }

    @Test
    public void shouldNotUpdateMotivationsWithInvalidMotivation() throws JsonProcessingException {
        setupMockRestServiceServer();

        var requestBody = List.of(
                new AssociateMotivationNeedRequest(UUID.fromString("11111111-1111-1111-1111-111111111111"), UUID.fromString("38731914-701d-4b4e-abd3-1e0a6375f0b2")),
                new AssociateMotivationNeedRequest(UUID.fromString("22222222-2222-2222-2222-222222222222"), UUID.fromString("00000000-0000-0000-0000-000000000000"))
        );

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplan/{0}/motivations", SENTENCE_PLAN_ID)
                .then()
                .statusCode(500)
                .extract().statusCode();

        assertThat(result).isEqualTo(500);

    }

    @Test
    public void shouldUpdateStepPriority() throws JsonProcessingException {

        setupMockRestServiceServer();

        var requestBody = List.of(new UpdateStepPriorityRequest(UUID.fromString("11111111-1111-1111-1111-111111111111"), 1));

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplan/{0}/steps/priority", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(UpdateStepPriorityRequest[].class);

        var result1 = result[0];
        assertThat(result1.getStepUUID()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(result1.getPriority()).isEqualTo(1);
    }

    @Test
    public void shouldUpdateServiceUserComment() throws JsonProcessingException {
        setupMockRestServiceServer();

        var requestBody = "I didn't done do it";

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplan/{0}/serviceUserComments", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract().statusCode();

        assertThat(result).isEqualTo(200);

        var plan = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplan/{0}", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(SentencePlan.class);

        assertThat(plan.getUuid()).isEqualTo(UUID.fromString(SENTENCE_PLAN_ID));
        assertThat(plan.getServiceUserComments()).isEqualTo(requestBody);
    }

    private MockRestServiceServer setupMockRestServiceServer() throws JsonProcessingException {
        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();

        var needs = List.of(new AssessmentNeed("Alcohol", true, true, true, true),
                new AssessmentNeed("Accommodation", true, true, true, true));

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123456/assessments/latest"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysAssessment(123456L, "ACTIVE", needs, true, true)), MediaType.APPLICATION_JSON));
        return assessmentApi;
    }

}