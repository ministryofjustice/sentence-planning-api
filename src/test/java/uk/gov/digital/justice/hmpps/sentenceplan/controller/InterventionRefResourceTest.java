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
import uk.gov.digital.justice.hmpps.sentenceplan.api.InterventionRef;
import uk.gov.digital.justice.hmpps.sentenceplan.api.MotivationRef;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.AssessmentNeed;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysRefElement;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.InterventionRefEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.InterventionRespository;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;

import java.util.Arrays;
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
@Sql(scripts = "classpath:intervention/before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:intervention/after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class InterventionRefResourceTest {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    OAuth2RestTemplate oauthRestTemplate;

    @Autowired
    InterventionRespository interventionRespository;

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
    public void shouldReturnActiveInterventionsData() {

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/interventions")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(InterventionRef[].class);

        assertThat(result).hasSize(3);

        assertThat(result[0].getUuid()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(result[0].getShortDescription()).isEqualTo("Inv 1");
        assertThat(result[0].getLongDescription()).isEqualTo("Intervention 1");

        assertThat(result[1].getUuid()).isEqualTo(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        assertThat(result[1].getShortDescription()).isEqualTo("Inv 2");
        assertThat(result[1].getLongDescription()).isEqualTo("Intervention 2");

        assertThat(result[2].getUuid()).isEqualTo(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        assertThat(result[2].getShortDescription()).isEqualTo("Inv 3");
        assertThat(result[2].getLongDescription()).isEqualTo("Intervention 3");

    }

    @Test
    public void shouldReturnAllInterventionsData() {

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/interventions/all")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(InterventionRef[].class);

        assertThat(result).hasSize(4);

        assertThat(result[0].getUuid()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(result[0].getShortDescription()).isEqualTo("Inv 1");
        assertThat(result[0].getLongDescription()).isEqualTo("Intervention 1");

        assertThat(result[1].getUuid()).isEqualTo(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        assertThat(result[1].getShortDescription()).isEqualTo("Inv 2");
        assertThat(result[1].getLongDescription()).isEqualTo("Intervention 2");

        assertThat(result[2].getUuid()).isEqualTo(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        assertThat(result[2].getShortDescription()).isEqualTo("Inv 3");
        assertThat(result[2].getLongDescription()).isEqualTo("Intervention 3");

        assertThat(result[3].getUuid()).isEqualTo(UUID.fromString("44444444-4444-4444-4444-444444444444"));
        assertThat(result[3].getShortDescription()).isEqualTo("Inv 4");
        assertThat(result[3].getLongDescription()).isEqualTo("Intervention 4");
    }

    @Test
    public void shouldReturnEmptyListWhenNoInterventionsAreActive() {

        interventionRespository.deleteAll(interventionRespository.findAllByActiveIsTrue());
        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/interventions")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(InterventionRef[].class);

        assertThat(result).hasSize(0);

    }

    @Test
    public void shouldReturnEmptyListWhenNoInterventions() {

        interventionRespository.deleteAll();
        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/interventions/all")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(InterventionRef[].class);

        assertThat(result).hasSize(0);

    }

    @Test
    public void shouldAddNewInterventionsAsActiveFromOasys() throws JsonProcessingException {

        createMockOasysInterventions();

        given()
            .when()
            .header("Accept", "application/json")
            .post("/interventions")
            .then()
            .statusCode(200);

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/interventions")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(InterventionRef[].class);

        assertThat(result).hasSize(3);

        var intervention1 = Arrays.stream(result).filter(i->i.getShortDescription().equals("Int 5")).findFirst().get();
        assertThat(intervention1.getLongDescription()).isEqualToIgnoringCase("Intervention 5");
        assertThat(intervention1.getUuid()).isNotNull();
    }

    @Test
    public void shouldUpdateExistingInterventionsFromOasys() throws JsonProcessingException {

        createMockOasysInterventions();

        given()
                .when()
                .header("Accept", "application/json")
                .post("/interventions")
                .then()
                .statusCode(200);

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/interventions")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(InterventionRef[].class);

        assertThat(result).hasSize(3);

        var intervention1 = Arrays.stream(result).filter(i->i.getUuid().equals(UUID.fromString("22222222-2222-2222-2222-222222222222"))).findFirst().get();
        assertThat(intervention1.getLongDescription()).isEqualToIgnoringCase("Updated Intervention 2");
        assertThat(intervention1.getShortDescription()).isEqualToIgnoringCase("Updated Int 2");

        var intervention2 = Arrays.stream(result).filter(i->i.getUuid().equals(UUID.fromString("33333333-3333-3333-3333-333333333333"))).findFirst().get();
        assertThat(intervention2.getLongDescription()).isEqualToIgnoringCase("Updated Intervention 3");
        assertThat(intervention2.getShortDescription()).isEqualToIgnoringCase("Updated Int 3");

    }

    @Test
    public void shouldMakeMissingInterventionsInActiveAndNotReturn() throws JsonProcessingException {

        createMockOasysInterventions();

        given()
                .when()
                .header("Accept", "application/json")
                .post("/interventions")
                .then()
                .statusCode(200);

        var result = given()
                .when()
                .header("Accept", "application/json")
                .get("/interventions")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(InterventionRef[].class);

        assertThat(result).hasSize(3);

        var intervention1 = Arrays.stream(result).filter(i->i.getUuid().equals(UUID.fromString("11111111-1111-1111-1111-111111111111"))).findFirst();
        assertThat(intervention1).isEmpty();

        var intervention2 = Arrays.stream(result).filter(i->i.getUuid().equals(UUID.fromString("22222222-2222-2222-2222-222222222222"))).findFirst();
        assertThat(intervention2).isNotEmpty();

    }

    private MockRestServiceServer createMockOasysInterventions() throws JsonProcessingException {
        var assessmentApi = bindTo(oauthRestTemplate).ignoreExpectOrder(true).build();

        var oasysInterventions = List.of(
                new OasysRefElement("INV2" ,"Updated Int 2", "Updated Intervention 2"),
                new OasysRefElement("INV3" ,"Updated Int 3", "Updated Intervention 3"),
                new OasysRefElement("INV5" ,"Int 5", "Intervention 5")
        );
        assessmentApi.expect(requestTo("http://localhost:8081/referencedata/INTERVENTION"))
                .andExpect(method(GET))
                .andRespond(withSuccess(mapper.writeValueAsString(oasysInterventions), MediaType.APPLICATION_JSON));
        return assessmentApi;
    }

}