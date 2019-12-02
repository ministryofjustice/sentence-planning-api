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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.util.Collections.EMPTY_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
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
public class SentenceBoardReviewResourceTest {


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

    private final String SBR_ID = "11111111-4444-4444-4444-111111111111";


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
    public void shouldGetSentenceBoardReviewSummaries() throws JsonProcessingException {

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplans/{sentencePlanUUID}/reviews", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", SentenceBoardReviewSummary.class);

        assertThat(result.get(0).getId().toString()).isEqualTo(SBR_ID);
        assertThat(result.get(0).getDateOfBoard()).isEqualTo(LocalDate.of(2019,11,14));

    }

    @Test
    public void shouldGetSentenceBoardReview() throws JsonProcessingException {

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplans/{sentencePlanUUID}/reviews/{sentenceBoardReviewUUID}", SENTENCE_PLAN_ID, SBR_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(SentenceBoardReview.class);

        assertThat(result.getId().toString()).isEqualTo(SBR_ID);
        assertThat(result.getDateOfBoard()).isEqualTo(LocalDate.of(2019,11,14));
        assertThat(result.getAttendees()).isEqualTo("Any Attendees");
        assertThat(result.getComments()).isEqualTo("Any Comments");
    }

    @Test
    public void shouldCreateSentenceBoardReview() throws JsonProcessingException {

                given()
                .when()
                .body(new AddSentenceBoardReviewRequest("any", "any", LocalDate.now()))
                .header("Content-Type", "application/json")
                .post("/sentenceplans/{sentencePlanUUID}/reviews", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200);

        var response = given()
                .when()
                .header("Accept", "application/json")
                .get("/sentenceplans/{sentencePlanUUID}/reviews", SENTENCE_PLAN_ID)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", SentenceBoardReviewSummary.class);

        assertThat(response.size()).isEqualTo(2);
    }
}