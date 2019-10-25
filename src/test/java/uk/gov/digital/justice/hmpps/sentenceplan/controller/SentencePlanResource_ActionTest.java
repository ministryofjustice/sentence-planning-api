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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.PRACTITIONER;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.SERVICE_USER;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:sentencePlan/before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:sentencePlan/after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class SentencePlanResource_ActionTest {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    OAuth2RestTemplate oauthRestTemplate;

    @Autowired
    SentencePlanRepository sentencePlanRepository;
    
    private final String SENTENCE_PLAN_ID = "11111111-1111-1111-1111-111111111111";
    private final String ACTION_ID = "11111111-1111-1111-1111-111111111111";
    private final String NOT_FOUND_ACTION_ID = "00000000-0000-0000-0000-000000000000";
    private final String EMPTY_ACTIONS_SENTENCE_PLAN_ID = "22222222-2222-2222-2222-222222222222";
    private final String NOT_FOUND_SENTENCE_PLAN_ID = "99999999-9999-9999-9999-999999999999";

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> mapper
        ));
    }


    @Test
    public void shouldCreateActionOnExistingPlan() {

        var requestBody = new AddSentencePlanAction(List.of(SERVICE_USER),
                null,
                "a strength",
                "a description",
                null,
                List.of(UUID.fromString("11111111-1111-1111-1111-111111111111")));

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplan/{0}/actions", SENTENCE_PLAN_ID)
                .then()
                .statusCode(201)
                .extract()
                .body()
                .jsonPath().getList(".", Action.class);

        assertThat(result).hasSize(2);
        var action = result.get(1);
        assertThat(action.getDescription()).isEqualTo("a description");
        assertThat(action.getStrength()).isEqualTo("a strength");
        assertThat(action.getIntervention()).isNull();
        assertThat(action.getOwnerOther()).isNull();
        assertThat(action.getOwner()).hasSize(1);
        assertThat(action.getOwner()).contains(SERVICE_USER);
    }

    @Test
    public void shouldGetActionsWhenSentencePlanExists() {

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplan/{0}/actions", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", Action.class);

        assertThat(result).hasSize(1);
        var action = result.get(0);
        assertThat(action.getDescription()).isEqualTo("description");
        assertThat(action.getStrength()).isEqualTo("strength");
        assertThat(action.getIntervention()).isNull();
        assertThat(action.getOwnerOther()).isNull();
        assertThat(action.getOwner()).hasSize(1);
        assertThat(action.getOwner()).contains(PRACTITIONER);

    }


    @Test
    public void shouldGetSingleActionWhenSentencePlanExists() {

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplan/{0}/actions/{1}", SENTENCE_PLAN_ID, ACTION_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Action.class);

        assertThat(result.getDescription()).isEqualTo("description");
        assertThat(result.getStrength()).isEqualTo("strength");
        assertThat(result.getIntervention()).isNull();
        assertThat(result.getOwnerOther()).isNull();
        assertThat(result.getOwner()).hasSize(1);
        assertThat(result.getOwner()).contains(PRACTITIONER);
    }

    @Test
    public void shouldReturnNotFoundForNonexistentPlan() {
        var result = given()
                .when()
                .get("/sentenceplan/{0}/actions", NOT_FOUND_SENTENCE_PLAN_ID)
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
    public void shouldGetEmptyArrayWhenNoActionsExist() {

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplan/{0}/actions", EMPTY_ACTIONS_SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", Action.class);

        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldUpdateActionOnExistingPlan() {

        var requestBody = new UpdateSentencePlanActionRequest(List.of(SERVICE_USER), ActionStatus.COMPLETED, null, "strong", "desc", null, List.of(UUID.randomUUID()));

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .put("/sentenceplan/{0}/actions/{1}", SENTENCE_PLAN_ID, ACTION_ID)
                .then()
                .statusCode(200)
                .extract().statusCode();

        assertThat(result).isEqualTo(200);

        var updatedAction = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplan/{0}/actions/{1}", SENTENCE_PLAN_ID, ACTION_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Action.class);

        assertThat(updatedAction.getDescription()).isEqualTo("desc");
        assertThat(updatedAction.getStrength()).isEqualTo("strong");
        assertThat(updatedAction.getIntervention()).isNull();
        assertThat(updatedAction.getOwnerOther()).isNull();
        assertThat(updatedAction.getOwner()).hasSize(1);
        assertThat(updatedAction.getOwner()).contains(SERVICE_USER);
    }

    @Test
    public void shouldNotUpdateInvalidAction() {

        var requestBody = new UpdateSentencePlanActionRequest(List.of(SERVICE_USER), ActionStatus.COMPLETED, null, "strong", "desc", null, List.of(UUID.randomUUID()));

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .put("/sentenceplan/{0}/actions/{1}", SENTENCE_PLAN_ID, NOT_FOUND_ACTION_ID)
                .then()
                .statusCode(404)
                .extract().statusCode();

        assertThat(result).isEqualTo(404);
    }

    @Test
    public void shouldProgressAction() {

        var requestBody = new ProgressActionRequest(ActionStatus.PARTIALLY_COMPLETED, "He didn't done do it");

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplan/{0}/actions/{1}/progress", SENTENCE_PLAN_ID, ACTION_ID)
                .then()
                .statusCode(200)
                .extract().statusCode();

        assertThat(result).isEqualTo(200);

        var progressedAction = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplan/{0}/actions/{1}", SENTENCE_PLAN_ID, ACTION_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Action.class);

        assertThat(progressedAction.getProgressList()).hasSize(1);
        assertThat(progressedAction.getProgressList().get(0).getStatus()).isEqualTo(ActionStatus.PARTIALLY_COMPLETED);
        assertThat(progressedAction.getProgressList().get(0).getPractitionerComments()).isEqualTo("He didn't done do it");

        assertThat(progressedAction.getUpdated()).isEqualTo(progressedAction.getProgressList().get(0).getCreated());
    }

    @Test
    public void shouldNotProgressInvalidAction() {

        var requestBody = new UpdateSentencePlanActionRequest(List.of(SERVICE_USER), ActionStatus.COMPLETED, null, "strong", "desc", null, List.of(UUID.randomUUID()));

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .post("/sentenceplan/{0}/actions/{1}/progress", SENTENCE_PLAN_ID, NOT_FOUND_ACTION_ID)
                .then()
                .statusCode(404)
                .extract().statusCode();

        assertThat(result).isEqualTo(404);
    }

}