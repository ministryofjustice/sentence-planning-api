package uk.gov.digital.justice.hmpps.sentenceplan.controller;


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
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.application.RequestData;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

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
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.PRACTITIONER;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.SERVICE_USER;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus.COMPLETED;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus.IN_PROGRESS;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:action/before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:action/after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class SentencePlanResource_ActionTest {

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
    private final Long OFFENDER_FULL = 123456L;
    private final Long OFFENDER_EMPTY = 789123L;
    private final String ACTION_ID = "0554387d-a19f-4cca-9443-5eb5e339709d";
    private final String NOT_FOUND_ACTION_ID = "00000000-0000-0000-0000-000000000000";
    private final String NOT_FOUND_SENTENCE_PLAN_ID = "99999999-9999-9999-9999-999999999999";
    public static final UUID MOTIVATION_UUID = UUID.fromString("38731914-701d-4b4e-abd3-1e0a6375f0b2");
    private final String USER = "TEST_USER";

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
    public void shouldCreateActionOnExistingPlan() {
        createMockAuthService(OFFENDER_EMPTY);
        var requestBody = new AddSentencePlanAction(
                null,
                "new action description",
                YearMonth.of(2019,11),
                UUID.fromString("38731914-701d-4b4e-abd3-1e0a6375f0b2"),
                List.of(SERVICE_USER),
                null,
                IN_PROGRESS);

                given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                        .header(RequestData.USERNAME_HEADER, USER)
                .post("/sentenceplans/{0}/objectives/{1}/actions", SENTENCE_PLAN_ID_EMPTY, OBJECTIVE_ID)
                .then()
                .statusCode(200);

        var result = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}/objectives/{1}", SENTENCE_PLAN_ID_EMPTY, OBJECTIVE_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Objective.class);

        assertThat(result.getActions()).hasSize(1);
        var action = result.getActions().stream().filter(a->a.getDescription().endsWith("new action description")).findAny().get();
        assertThat(action.getStatus()).isEqualTo(IN_PROGRESS);
        assertThat(action.getIntervention()).isNull();
        assertThat(action.getOwnerOther()).isNull();
        assertThat(action.getOwner()).hasSize(1);
        assertThat(action.getOwner()).contains(SERVICE_USER);
    }

    @Test
    public void shouldGetSingleActionWhenSentencePlanExists() {
        createMockAuthService(OFFENDER_FULL);
        var result = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}/objectives/{1}/actions/{2}", SENTENCE_PLAN_ID_FULL, OBJECTIVE_ID, ACTION_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Action.class);

        assertThat(result.getDescription()).isEqualTo("Action 1");
        assertThat(result.getIntervention()).isNull();
        assertThat(result.getOwnerOther()).isNull();
        assertThat(result.getOwner()).hasSize(1);
        assertThat(result.getStatus()).isEqualTo(IN_PROGRESS);
        assertThat(result.getOwner()).contains(SERVICE_USER);
        assertThat(result.getPriority()).isEqualTo(0);
    }

    @Test
    public void shouldReturnNotFoundForNonexistentPlan() {
        createMockAuthService(OFFENDER_FULL);
        var result = given()
                .when()
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}/objectives/{1}/actions/{2}", NOT_FOUND_SENTENCE_PLAN_ID, OBJECTIVE_ID, ACTION_ID)
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
    public void shouldUpdateActionOnExistingPlan() {
        createMockAuthService(OFFENDER_FULL);
        var requestBody = new AddSentencePlanAction(
                null,
                "updated action description",
                YearMonth.of(2019,11),
                UUID.fromString("38731914-701d-4b4e-abd3-1e0a6375f0b2"),
                List.of(PRACTITIONER),
                null,
                COMPLETED);
        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .put("/sentenceplans/{0}/objectives/{1}/actions/{2}", SENTENCE_PLAN_ID_FULL,OBJECTIVE_ID, ACTION_ID)
                .then()
                .statusCode(200)
                .extract().statusCode();

        assertThat(result).isEqualTo(200);

        var updatedAction = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}/objectives/{1}/actions/{2}", SENTENCE_PLAN_ID_FULL,OBJECTIVE_ID, ACTION_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Action.class);

        assertThat(updatedAction.getDescription()).isEqualTo("updated action description");
        assertThat(updatedAction.getIntervention()).isNull();
        assertThat(updatedAction.getOwnerOther()).isNull();
        assertThat(updatedAction.getOwner()).hasSize(1);
        assertThat(updatedAction.getOwner()).contains(PRACTITIONER);
    }

    @Test
    public void shouldNotUpdateInvalidAction() {
        createMockAuthService(OFFENDER_FULL);
        var requestBody = new AddSentencePlanAction(
                null,
                "updated action description",
                YearMonth.of(2019,11),
                MOTIVATION_UUID,
                List.of(PRACTITIONER),
                null,
                COMPLETED);

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .put("/sentenceplans/{0}/objectives/{1}/actions/{2}", SENTENCE_PLAN_ID_FULL,OBJECTIVE_ID, NOT_FOUND_ACTION_ID)
                .then()
                .statusCode(404)
                .extract().statusCode();

        assertThat(result).isEqualTo(404);
    }

    @Test
    public void shouldProgressAction() {
        createMockAuthService(OFFENDER_FULL);
        var requestBody = new ProgressActionRequest(ActionStatus.PARTIALLY_COMPLETED, YearMonth.of(2019,12),MOTIVATION_UUID,"new test comment" );

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .post("/sentenceplans/{0}/objectives/{1}/actions/{2}/progress", SENTENCE_PLAN_ID_FULL, OBJECTIVE_ID, ACTION_ID)
                .then()
                .statusCode(200)
                .extract().statusCode();

        assertThat(result).isEqualTo(200);

        var progressedAction = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}/objectives/{1}/actions/{2}", SENTENCE_PLAN_ID_FULL, OBJECTIVE_ID, ACTION_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Action.class);

        assertThat(progressedAction.getProgress()).hasSize(1);
        var actionProgress = progressedAction.getProgress().stream().filter(p -> p.getComment().equals("new test comment" )).findAny();
        assertThat(actionProgress.get().getStatus()).isEqualTo(ActionStatus.PARTIALLY_COMPLETED);
        assertThat(actionProgress.get().getTargetDate()).isEqualTo(YearMonth.of(2019,12));
        assertThat(actionProgress.get().getMotivationUUID()).isEqualTo(MOTIVATION_UUID);
    }

    @Test
    public void shouldNotProgressInvalidAction() {
        createMockAuthService(OFFENDER_FULL);
        var requestBody = new ProgressActionRequest(ActionStatus.PARTIALLY_COMPLETED, YearMonth.of(2019,12),MOTIVATION_UUID,"new test comment" );

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .post("/sentenceplans/{0}/objectives/{1}/actions/{2}/progress", SENTENCE_PLAN_ID_FULL, OBJECTIVE_ID, NOT_FOUND_ACTION_ID)
                .then()
                .statusCode(404)
                .extract().statusCode();

        assertThat(result).isEqualTo(404);
    }

    private void createMockAuthService(Long offenderId, MockRestServiceServer assessmentApi) {
        assessmentApi.expect(between(1,2), requestTo("http://localhost:8081/authentication/user/" + USER + "/offender/" + offenderId))
                .andExpect(method(GET))
                .andRespond(withSuccess());
    }

    private void createMockAuthService(Long offenderId) {
        bindTo(oauthRestTemplate).ignoreExpectOrder(true).build().expect(between(1,2), requestTo("http://localhost:8081/authentication/user/" + USER + "/offender/" + offenderId))
                .andExpect(method(GET))
                .andRespond(withSuccess());
    }

}