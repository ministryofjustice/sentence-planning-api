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
import uk.gov.digital.justice.hmpps.sentenceplan.api.Motivation;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.AssessmentNeed;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysIdentifiers;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;

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

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:motivation/before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:motivation/after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class MotivationResourceTest {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    OAuth2RestTemplate oauthRestTemplate;

    @Autowired
    SentencePlanRepository sentencePlanRepository;

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
    public void shouldReturnData() {

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/motivation")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Motivation[].class);

        assertThat(result).hasSize(1);
        Motivation motivation = result[0];

        assertThat(motivation.getUUID()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(motivation.getMotivationText()).isEqualTo("Motivation");
        assertThat(motivation.getFriendlyText()).isEqualTo("Friendly");

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