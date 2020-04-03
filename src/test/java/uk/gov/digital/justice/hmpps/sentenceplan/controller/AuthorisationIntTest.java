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
import uk.gov.digital.justice.hmpps.sentenceplan.api.ErrorResponse;
import uk.gov.digital.justice.hmpps.sentenceplan.application.RequestData;
import uk.gov.digital.justice.hmpps.sentenceplan.client.SectionHeader;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.AssessmentNeed;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAuthorisationDto;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;

import java.util.List;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.client.ExpectedCount.between;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffenderPermissionLevel.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffenderPermissionResource.SENTENCE_PLAN;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:sentencePlan/before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:sentencePlan/after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class AuthorisationIntTest {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    OAuth2RestTemplate oauthRestTemplate;

    private final String SENTENCE_PLAN_ID = "11111111-1111-1111-1111-111111111111";
    private final String USER = "TEST_USER";
    private final String SESSION_ID = "123456";
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
    public void shouldReturn200IfUserIsAuthorisedToAccessSentencePlan() throws JsonProcessingException {

        createAuthorisedMockAuthService();
        given()
            .when()
            .header("Accept", "application/json")
            .header(RequestData.USERNAME_HEADER, USER)
                .header(RequestData.SESSION_ID_HEADER, SESSION_ID)
            .get("/sentenceplans/{0}", SENTENCE_PLAN_ID)
            .then()
            .statusCode(200);
    }

    @Test
    public void shouldReturn401IfUserIsNotAuthorisedToAccessSentencePlan() throws JsonProcessingException {

        createNotAuthorisedMockAuthService();
        given()
            .when()
            .header("Accept", "application/json")
            .header(RequestData.USERNAME_HEADER, USER)
                .header(RequestData.SESSION_ID_HEADER, SESSION_ID)
            .get("/sentenceplans/{0}", SENTENCE_PLAN_ID)
            .then()
            .statusCode(401);
    }


    @Test
    public void shouldReturn404WhenOffenderNotFound() throws JsonProcessingException {
        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();
        assessmentApi.expect(between(1,2), requestTo("http://localhost:8081/authentication/user/" + USER + "/offender/123/SENTENCE_PLAN?sessionId=123456"))
                .andExpect(method(GET))
                .andRespond(withStatus(NOT_FOUND));

        given()
                .when()
                .header("Content-Type", "application/json")
                .header(RequestData.USERNAME_HEADER, USER)
                .header(RequestData.SESSION_ID_HEADER, SESSION_ID)
                .post("/offenders/{oasysOffenderId}/sentenceplans", 123L)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

    }

    private MockRestServiceServer createMockAssessmentDataForOffender(Long offenderId) throws JsonProcessingException {
        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/" + offenderId))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysOffender(123456L, null, null, null, null, "Nomis", "4", null, null)), MediaType.APPLICATION_JSON));

        var needs = List.of(new AssessmentNeed(SectionHeader.ALCOHOL_MISUSE, "Alcohol", true, true, true, true),
                new AssessmentNeed(SectionHeader.ALCOHOL_MISUSE,"Accommodation", true, true, true, true));

        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/" + offenderId + "/assessments/latest?assessmentType=LAYER_3"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysAssessment(123456L, "ACTIVE", needs, true)), MediaType.APPLICATION_JSON));

        return assessmentApi;
    }

    private void createAuthorisedMockAuthService() throws JsonProcessingException {
        var asssessmentApi = createMockAssessmentDataForOffender(123456L);

        asssessmentApi.expect(between(1,2), requestTo("http://localhost:8081/authentication/user/" + USER + "/offender/" + 123456L + "/SENTENCE_PLAN?sessionId=" + 123456L))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysAuthorisationDto("USER", 123456L, WRITE, SENTENCE_PLAN)), MediaType.APPLICATION_JSON));
    }

    private void createNotAuthorisedMockAuthService() throws JsonProcessingException {
        var asssessmentApi = createMockAssessmentDataForOffender(123456L);

        asssessmentApi.expect(between(1,2), requestTo("http://localhost:8081/authentication/user/" + USER + "/offender/" + 123456L + "/SENTENCE_PLAN?sessionId=" + 123456L))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new OasysAuthorisationDto("USER", 123456L, UNAUTHORISED, SENTENCE_PLAN)), MediaType.APPLICATION_JSON));
    }
}