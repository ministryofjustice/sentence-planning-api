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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test,disableauthorisation")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:sentencePlan/before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:sentencePlan/after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class SentencePlanResourceTest {

    /*
        before-test.sql sets up a sentence plan with:
        9 Needs:
        - Thinking and Behaviour
        - Attitudes
        - Accommodation
        - Education, Training and Employability
        - Financial Management and Income
        - Relationships
        - Lifestyle and Associates
        - Alcohol Misuse
        - Emotional Well-Being

        2 Comments:
        - YOUR_RESPONSIVITY
        - LIAISON_ARRANGEMENTS

        2 Objectives:
        - Objective 1 with 2 Actions
        - Objective 2 with 2 Actions with progress

        1 Sentence Board Review

     */

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
    public void shouldGetSentencePlanWhenExists() throws JsonProcessingException {
        var assessmentApi = createMockAssessmentDataForOffender(OASYS_OFFENDER_ID);
        var result = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(SentencePlanDto.class);

        assertThat(result.getUuid()).isEqualTo(UUID.fromString(SENTENCE_PLAN_ID));
    }

    @Test
    public void shouldGetSentencePlanSummaries() throws JsonProcessingException {

        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123456/fullSentencePlans"))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(List.of(
                            new OasysSentencePlanDto(12345L, LocalDate.of(2010, 1,1), null, Collections.emptyList(), Collections.emptyMap())
                        )), MediaType.APPLICATION_JSON));

        var result = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/offenders/{0}/sentenceplans/", OASYS_OFFENDER_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", SentencePlanSummaryDto.class);

        assertThat(result.get(0).getPlanId()).isEqualTo(SENTENCE_PLAN_ID);
        assertThat(result.get(0).getCreatedDate()).isEqualTo(LocalDate.of(2019,11,14));

        assertThat(result.get(1).getPlanId()).isEqualTo("12345");
        assertThat(result.get(1).getCreatedDate()).isEqualTo(LocalDate.of(2010,1,1));

    }


    @Test
    public void shouldGetLegacySentencePlanIfExists() throws JsonProcessingException {

        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();
        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123456/fullSentencePlans/12345"))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(
                                new OasysSentencePlanDto(12345L, LocalDate.of(2010, 1,1), null, Collections.emptyList(), Collections.emptyMap())
                        ), MediaType.APPLICATION_JSON));

        var result = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/offenders/{0}/sentenceplans/{1}", OASYS_OFFENDER_ID, 12345L)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OasysSentencePlanDto.class);

        assertThat(result.getOasysSetId()).isEqualTo(12345L);
    }

    @Test
    public void shouldReturnNotFoundForNonexistentPlan() {
        var result = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}", NOT_FOUND_SENTENCE_PLAN_ID)
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

        var assessmentApi = createMockAssessmentDataForOffender(123L);
        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123/summary"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysOffender(123L, "Gary", "Smith", "", "", new OasysIdentifiers("12345678", "123"))), MediaType.APPLICATION_JSON));

        given()
            .when()
            .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
            .post("/offenders/{oasysOffenderId}/sentenceplans", 123L)
            .then()
            .statusCode(201);

        assessmentApi.verify();
    }

    @Test
    public void shouldCreateNewDraftSentencePlan() throws JsonProcessingException {

        var assessmentApi = createMockAssessmentDataForOffender(123L);
        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123/summary"))
                    .andExpect(method(GET))
                    .andRespond(withSuccess(mapper.writeValueAsString(new OasysOffender(123L, "Gary", "Smith", "", "", new OasysIdentifiers("12345678", "123"))), MediaType.APPLICATION_JSON));

        var result = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .post("/offenders/{oasysOffenderId}/sentenceplans", 123L)
                .then()
                .statusCode(201)
                .extract()
                .body()
                .as(SentencePlanDto.class);

        assertThat(result.isDraft()).isTrue();
        assertThat(result.getObjectives().size()).isEqualTo(0);
        assertThat(result.getNeeds().size()).isEqualTo(2);
    }

    @Test
    public void shouldNotCreateNewSentencePlanIfCurrentPlanExistsForOffender() throws JsonProcessingException {

        var assessmentApi = createMockAssessmentDataForOffender(123L);
        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123/summary"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysOffender(123L, "Gary", "Smith", "", "", new OasysIdentifiers("12345678", "123"))), MediaType.APPLICATION_JSON));

            given()
                .when()
                .header("Content-Type", "application/json")
                    .header(RequestData.USERNAME_HEADER, USER)
                    .post("/offenders/{oasysOffenderId}/sentenceplans", 123L)
                .then()
                .statusCode(201)
                .extract()
                .body()
                .as(SentencePlanDto.class);

        var errorResult = given()
                .when()
                .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .post("/offenders/{oasysOffenderId}/sentenceplans", 123L)
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(errorResult.getStatus()).isEqualTo(400);
        assertThat(errorResult.getDeveloperMessage()).isEqualToIgnoringCase("Offender already has a current sentence plan");
    }

    @Test
    public void shouldReturn400WhenCreatingPlanWithoutAnAssessment() throws JsonProcessingException {

        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();
        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123/summary"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysOffender(123L, "Gary", "Smith", "", "", new OasysIdentifiers("12345678", "123"))), MediaType.APPLICATION_JSON));

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123/assessments/latest?assessmentType=LAYER_3"))
                .andExpect(method(GET))
                .andRespond(withStatus(NOT_FOUND));

        var result = given()
                .when()
                .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .post("/offenders/{oasysOffenderId}/sentenceplans", 123L)
                .then()
                .statusCode(400)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDeveloperMessage()).isEqualToIgnoringCase("Assessment not found for offender");

    }

    @Test
    public void shouldAddComments() throws JsonProcessingException {
        var assessmentApi = createMockAssessmentDataForOffender(123456L);
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

        var plan = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(SentencePlanDto.class);

        assertThat(plan.getUuid()).isEqualTo(UUID.fromString(SENTENCE_PLAN_ID));
        assertThat(plan.getComments()).hasSize(3); //two added in before-test.sql
        var createdComment = plan.getComments().stream().filter(c->c.getCommentType().equals(CommentType.THEIR_SUMMARY)).findAny();
        assertThat(createdComment.get().getComment()).isEqualToIgnoringCase(comment.getComment());
    }


    @Test
    public void shouldGetComments() throws JsonProcessingException {
        createMockAssessmentDataForOffender(OASYS_OFFENDER_ID);
        var comments = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}/comments", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body().jsonPath().getList(".", CommentDto.class);

        assertThat(comments).hasSize(2);
        var comment1 = comments.stream().filter(c->c.getCommentType().equals(CommentType.YOUR_RESPONSIVITY)).findAny();
        var comment2 = comments.stream().filter(c->c.getCommentType().equals(CommentType.LIAISON_ARRANGEMENTS)).findAny();

        assertThat(comment1.get().getComment()).isEqualTo("a comment");
        assertThat(comment2.get().getComment()).isEqualTo("another comment");

    }

    @Test
    public void shouldGetCommentsAddingOverwrites() throws JsonProcessingException {
        createMockAssessmentDataForOffender(OASYS_OFFENDER_ID);
        var newComment = new AddCommentRequest("Any Comment", CommentType.LIAISON_ARRANGEMENTS);
        var requestBody = List.of(newComment);

        var newComment1 = new AddCommentRequest("Any New Comment", CommentType.LIAISON_ARRANGEMENTS);
        var requestBody1 = List.of(newComment1);

        given()
            .when()
            .body(requestBody)
            .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .put("/sentenceplans/{0}/comments", SENTENCE_PLAN_ID)
            .then()
            .statusCode(200)
            .extract().statusCode();

        given()
            .when()
            .body(requestBody1)
            .header("Content-Type", "application/json")
               .header(RequestData.USERNAME_HEADER, USER)
               .put("/sentenceplans/{0}/comments", SENTENCE_PLAN_ID)
            .then()
            .statusCode(200)
            .extract().statusCode();

        var comments = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}/comments", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body().jsonPath().getList(".", CommentDto.class);

        assertThat(comments).hasSize(2);
        var comment = comments.stream().filter(c->c.getCommentType().equals(CommentType.LIAISON_ARRANGEMENTS)).findAny();
        assertThat(comment.get().getComment()).isEqualTo("Any New Comment");
    }

    @Test
    public void shouldCreateNewRevisionsOnChange() throws JsonProcessingException {
        var assessmentApi = createMockAssessmentDataForOffender(123L);
        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123/summary"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysOffender(123L, "Gary", "Smith", "", "", new OasysIdentifiers("12345678", "123"))), MediaType.APPLICATION_JSON));

        //create a new sentence plan
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


        var revisions = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}/revisions", sentencePlan.getUuid())
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getList(".", SentencePlanSummaryDto.class);

        //value is 1 after creation
        assertThat(revisions).hasSize(1);


        var comment = new AddCommentRequest("Test Comment", CommentType.THEIR_SUMMARY);
        var requestBody = List.of(comment);

        //add a comment to create an update and another revision
        given()
            .when()
            .body(requestBody)
            .header("Content-Type", "application/json")
            .header(RequestData.USERNAME_HEADER, USER)
            .put("/sentenceplans/{0}/comments", sentencePlan.getUuid())
            .then()
            .statusCode(200)
            .extract().statusCode();

        var afterUpdateRevisions = given()
                .when()
                .header("Accept", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .get("/sentenceplans/{0}/revisions", sentencePlan.getUuid())
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getList(".", SentencePlanSummaryDto.class);

        assertThat(afterUpdateRevisions).hasSize(2);

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