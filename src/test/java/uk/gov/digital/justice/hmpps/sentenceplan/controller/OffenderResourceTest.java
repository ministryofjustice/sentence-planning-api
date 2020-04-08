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
import uk.gov.digital.justice.hmpps.sentenceplan.client.SectionHeader;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.AssessmentNeed;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysSentencePlanDto;
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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@ActiveProfiles("test,disableauthorisation")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OffenderResourceTest {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    OAuth2RestTemplate oauthRestTemplate;

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
    public void shouldGetOffenderWhenExists() throws JsonProcessingException {
        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();
        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/123"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(
                        new OasysOffender(123L,false, "Offender",
                                "Mike",  "Tom", "Steve", "Y", "N","PNC",
                                "CRN", "NOMIS", "LEGACYCMS","CRO","booking",
                                "MPNC", 321L)), MediaType.APPLICATION_JSON));

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/offenders/oasysOffenderId/{0}", 123L)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OasysOffender.class);

        assertThat(result.getOasysOffenderId()).isEqualTo(123L);
        assertThat(result.isLimitedAccessOffender()).isFalse();
        assertThat(result.getFamilyName()).isEqualTo("Offender");
        assertThat(result.getForename1()).isEqualTo("Mike");
        assertThat(result.getForename2()).isEqualTo("Tom");
        assertThat(result.getForename3()).isEqualTo("Steve");
        assertThat(result.getRiskToOthers()).isEqualTo("Y");
        assertThat(result.getRiskToSelf()).isEqualTo("N");
        assertThat(result.getPnc()).isEqualTo("PNC");
        assertThat(result.getCrn()).isEqualTo("CRN");
        assertThat(result.getNomisId()).isEqualTo("NOMIS");
        assertThat(result.getLegacyCmsProbNumber()).isEqualTo("LEGACYCMS");
        assertThat(result.getCroNumber()).isEqualTo("CRO");
        assertThat(result.getBookingNumber()).isEqualTo("booking");
        assertThat(result.getMergePncNumber()).isEqualTo("MPNC");
        assertThat(result.getMergedOasysOffenderId()).isEqualTo(321L);
    }

    @Test
    public void shouldReturnNotFoundForNonexistentOffender() {
        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();
        assessmentApi.expect(requestTo("http://localhost:8081/offenders/oasysOffenderId/321"))
                .andExpect(method(GET))
                .andRespond(withStatus(NOT_FOUND));

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/offenders/oasysOffenderId/{0}", 321L)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getStatus()).isEqualTo(404);
    }
}