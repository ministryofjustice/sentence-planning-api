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
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.*;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;

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
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:sentencePlan/before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:sentencePlan/after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class TimelineResourceTest {

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
    public void shouldGetTimelineEntitySpNotFound() throws JsonProcessingException {
        var assessmentApi = createMockAssessmentDataForOffender(123456L);
        createMockAuthService(OASYS_OFFENDER_ID, assessmentApi);
        var comment = new AddCommentRequest("Test Comment", CommentType.THEIR_SUMMARY);
        var requestBody = List.of(comment);


        var timeline = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/timeline/sentenceplans/{0}/entity/THEIR_SUMMARY", NOT_FOUND_SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", TimelineDto.class);

        assertThat(timeline).hasSize(0);
    }

    @Test
    public void shouldGetTimelineEntityNotFound() throws JsonProcessingException {
        var assessmentApi = createMockAssessmentDataForOffender(123456L);
        createMockAuthService(OASYS_OFFENDER_ID, assessmentApi);
        var comment = new AddCommentRequest("Test Comment", CommentType.THEIR_SUMMARY);
        var requestBody = List.of(comment);


        var timeline = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/timeline/sentenceplans/{0}", NOT_FOUND_SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", TimelineDto.class);

        assertThat(timeline).hasSize(0);
    }

    @Test
    public void shouldGetTimelineEntityEntityNotFound() throws JsonProcessingException {
        var assessmentApi = createMockAssessmentDataForOffender(123456L);
        createMockAuthService(OASYS_OFFENDER_ID, assessmentApi);
        var comment = new AddCommentRequest("Test Comment", CommentType.THEIR_SUMMARY);
        var requestBody = List.of(comment);

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .put("/sentenceplans/{0}/comments", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract().statusCode();

        assertThat(result).isEqualTo(200);

        var timeline = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/timeline/sentenceplans/{0}/entity/FAKE_TEXT_SUMMARY", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", TimelineDto.class);

        assertThat(timeline).hasSize(0);
    }

    @Test
    public void shouldAddCommentsTimeline() throws JsonProcessingException {
        var assessmentApi = createMockAssessmentDataForOffender(123456L);
        createMockAuthService(OASYS_OFFENDER_ID, assessmentApi);
        var comment = new AddCommentRequest("Test Comment", CommentType.THEIR_SUMMARY);
        var requestBody = List.of(comment);

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .put("/sentenceplans/{0}/comments", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract().statusCode();

        assertThat(result).isEqualTo(200);

        var timeline = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/timeline/sentenceplans/{0}/entity/THEIR_SUMMARY", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", TimelineDto.class);

        assertThat(timeline).hasSize(1);
        var comment1 = timeline.stream().findFirst().get();

        assertThat(comment1.getComment().getComment()).isEqualTo(comment.getComment());
        assertThat(comment1.getComment().getCommentType()).isEqualTo(comment.getCommentType());
        assertThat(comment1.getUserName()).isEqualTo(USER);
        assertThat(comment1.getTimelineType()).isEqualTo("COMMENT");
    }

    @Test
    public void shouldAddObjectiveTimeline() throws JsonProcessingException {
        var assessmentApi = createMockAssessmentDataForOffender(123456L);
        createMockAuthService(OASYS_OFFENDER_ID, assessmentApi);
        var needs = List.of(UUID.fromString("9acddbd3-af5e-4b41-a710-018064700eb5"),
                UUID.fromString("51c293ec-b2c4-491c-ade5-34375e1cd495"));
        var requestBody = new AddSentencePlanObjectiveRequest(
                "new objective description",
                needs, false);

        ObjectiveDto result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .post("/sentenceplans/{0}/objectives", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body().as(ObjectiveDto.class);


        var timeline = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/timeline/sentenceplans/{0}/entity/{1}", SENTENCE_PLAN_ID, result.getId())
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", TimelineDto.class);

        assertThat(timeline).hasSize(1);
        var objective = timeline.stream().findFirst().get();

        assertThat(objective.getObjective().getDescription()).isEqualTo(requestBody.getDescription());
        assertThat(objective.getObjective().isMeetsChildSafeguarding()).isEqualTo(requestBody.isMeetsChildSafeguarding());
        assertThat(objective.getObjective().getNeeds()).hasSize(2);
        assertThat(objective.getUserName()).isEqualTo(USER);
        assertThat(objective.getTimelineType()).isEqualTo("OBJECTIVE");

    }

    @Test
    public void shouldAddCommentsAndObjectivesTimeline() throws JsonProcessingException {
        var assessmentApi = createMockAssessmentDataForOffender(123456L);
        createMockAuthService(OASYS_OFFENDER_ID, assessmentApi);
        var comment = new AddCommentRequest("Test Comment", CommentType.THEIR_SUMMARY);
        var requestBody = List.of(comment);

        var result = given()
                .when()
                .body(requestBody)
                .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .put("/sentenceplans/{0}/comments", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract().statusCode();

        assertThat(result).isEqualTo(200);

        var needs = List.of(UUID.fromString("9acddbd3-af5e-4b41-a710-018064700eb5"),
                UUID.fromString("51c293ec-b2c4-491c-ade5-34375e1cd495"));
        var requestBody1 = new AddSentencePlanObjectiveRequest(
                "new objective description",
                needs, false);

        var result1 = given()
                .when()
                .body(requestBody1)
                .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .post("/sentenceplans/{0}/objectives", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract().statusCode();

        assertThat(result1).isEqualTo(200);

        var timeline = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/timeline/sentenceplans/{0}/", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", TimelineDto.class);

        assertThat(timeline).hasSize(2);

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

    private void createMockAuthService(Long offenderId, MockRestServiceServer assessmentApi) {
        assessmentApi.expect(between(1,4), requestTo("http://localhost:8081/authentication/user/" + USER + "/offender/" + offenderId))
                .andExpect(method(GET))
                .andRespond(withSuccess());
    }

}