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
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.AssessmentNeed;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.PRACTITIONER;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.SERVICE_USER;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus.COMPLETED;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus.IN_PROGRESS;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:objective/before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:objective/after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class SentencePlanResource_ObjectiveTest {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    OAuth2RestTemplate oauthRestTemplate;

    @Autowired
    SentencePlanRepository sentencePlanRepository;

    private final String OBJECTIVE_ID = "59023444-afda-4603-9284-c803d18ee4bb";
    private final String SENTENCE_PLAN_ID_FULL = "11111111-1111-1111-1111-111111111111";
    private final String SENTENCE_PLAN_ID_EMPTY = "22222222-2222-2222-2222-222222222222";
    private final String NOT_FOUND_OBJECTIVE_ID = "00000000-0000-0000-0000-000000000000";
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
    public void shouldCreateActionOnExistingPlan() throws JsonProcessingException {
        createMockAssessmentDataForOffender(789123L);
        var needs = List.of(UUID.fromString("9acddbd3-af5e-4b41-a710-018064700eb5"),
                UUID.fromString("51c293ec-b2c4-491c-ade5-34375e1cd495"));
        var requestBody = new AddSentencePlanObjective(
                "new objective description",
                needs);

                given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplans/{0}/objectives", SENTENCE_PLAN_ID_EMPTY)
                .then()
                .statusCode(200);

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplans/{0}", SENTENCE_PLAN_ID_EMPTY)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(SentencePlan.class);

        assertThat(result.getObjectives()).hasSize(1);
        var objective = result.getObjectives().stream().filter(a->a.getDescription().endsWith("new objective description")).findAny().get();
        assertThat(objective.getNeeds()).containsExactlyInAnyOrderElementsOf(needs);

    }

    @Test
    public void shouldGetSingleObjectiveWhenSentencePlanExists() {

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplans/{0}/objectives/{1}", SENTENCE_PLAN_ID_FULL, OBJECTIVE_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Objective.class);

        assertThat(result.getDescription()).isEqualTo("Objective 1");
        assertThat(result.getNeeds()).containsExactlyInAnyOrder(
                UUID.fromString("9acddbd3-af5e-4b41-a710-018064700eb5"), UUID.fromString("51c293ec-b2c4-491c-ade5-34375e1cd495"));
    }

    @Test
    public void shouldReturnNotFoundForNonexistentPlan() {
        var result = given()
                .when()
                .get("/sentenceplans/{0}/objectives/{1}", NOT_FOUND_SENTENCE_PLAN_ID, OBJECTIVE_ID)
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
    public void shouldUpdateObjectiveOnExistingPlan() throws JsonProcessingException {
        createMockAssessmentDataForOffender(123456L);
        var needs = List.of(UUID.fromString("9acddbd3-af5e-4b41-a710-018064700eb5"),
                UUID.fromString("51c293ec-b2c4-491c-ade5-34375e1cd495"));

        var requestBody = new AddSentencePlanObjective(
                "new objective description",
                needs);

        given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .put("/sentenceplans/{0}/objectives/{1}", SENTENCE_PLAN_ID_FULL,OBJECTIVE_ID)
                .then()
                .statusCode(200);

        var updatedObjective = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplans/{0}/objectives/{1}", SENTENCE_PLAN_ID_FULL,OBJECTIVE_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Objective.class);

        assertThat(updatedObjective.getDescription()).isEqualTo("new objective description");
        assertThat(updatedObjective.getNeeds()).containsExactlyInAnyOrder(
                UUID.fromString("9acddbd3-af5e-4b41-a710-018064700eb5"),
                UUID.fromString("51c293ec-b2c4-491c-ade5-34375e1cd495"));

    }

    @Test
    public void shouldUpdateObjectivePriority() throws JsonProcessingException {

        createMockAssessmentDataForOffender(123456L);

        var requestBody = List.of(
                new  UpdateObjectivePriorityRequest(UUID.fromString("59023444-afda-4603-9284-c803d18ee4bb"), 1),
                new  UpdateObjectivePriorityRequest(UUID.fromString("a63a8eac-4daf-4801-b32b-e3d20c249ad4"), 2)

        );

        given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplans/{0}/objectives/priority", SENTENCE_PLAN_ID_FULL)
                .then()
                .statusCode(200);

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplans/{0}", SENTENCE_PLAN_ID_FULL)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList("objectives", Objective.class);

        var objective1 = result.stream().filter(o->o.getId().equals(UUID.fromString("59023444-afda-4603-9284-c803d18ee4bb"))).findAny();
        var objective2 = result.stream().filter(o->o.getId().equals(UUID.fromString("a63a8eac-4daf-4801-b32b-e3d20c249ad4"))).findAny();
        assertThat(objective1.get().getPriority()).isEqualTo(1);
        assertThat(objective2.get().getPriority()).isEqualTo(2);
    }

    @Test
    public void shouldNotUpdateInvalidObjective() {
        var needs = List.of(UUID.fromString("9acddbd3-af5e-4b41-a710-018064700eb5"),
                UUID.fromString("51c293ec-b2c4-491c-ade5-34375e1cd495"));

        var requestBody = new AddSentencePlanObjective(
                "new objective description",
                needs);

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .put("/sentenceplans/{0}/objectives/{1}", SENTENCE_PLAN_ID_FULL, NOT_FOUND_OBJECTIVE_ID)
                .then()
                .statusCode(404);

    }

    private MockRestServiceServer createMockAssessmentDataForOffender(Long offenderId) throws JsonProcessingException {
        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();

        var needs = List.of(new AssessmentNeed("Alcohol", true, true, true, true),
                new AssessmentNeed("Accommodation", true, true, true, true));

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/"+ offenderId + "/assessments/latest?assessmentType=LAYER_3"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysAssessment(123456L, "ACTIVE", needs, true)), MediaType.APPLICATION_JSON));
        return assessmentApi;
    }
}