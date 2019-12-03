package uk.gov.digital.justice.hmpps.sentenceplan.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysRefElement;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysSentencePlan;
import uk.gov.digital.justice.hmpps.sentenceplan.client.exception.OasysClientException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
public class OASYSAssessmentAPIClient {

    private OAuth2RestTemplate restTemplate;
    private String assessmentApiBasePath;

    public OASYSAssessmentAPIClient(OAuth2RestTemplate restTemplate, @Value("${assessment.api.uri.root}") String assessmentApiBasePath) {
        this.restTemplate = restTemplate;
        this.assessmentApiBasePath = assessmentApiBasePath;
    }

    public Optional<OasysOffender> getOffenderById(long oasysOffenderId) {
        try {
            return Optional.ofNullable(restTemplate.getForEntity(assessmentApiBasePath + "/offenders/oasysOffenderId/{oasysOffenderId}/summary", OasysOffender.class, oasysOffenderId).getBody());
        }
        catch(HttpClientErrorException e) {
            if(e.getRawStatusCode() == 404) {
                log.info("Offender {} not found", oasysOffenderId, LogEvent.OASYS_ASSESSMENT_NOT_FOUND);
                return Optional.empty();
            }
            log.error("Failed to retrieve assessment for offender", LogEvent.OASYS_ASSESSMENT_CLIENT_FAILURE);
            throw new OasysClientException("Failed to retrieve assessment for offender");
        }
    }

    public Optional<OasysAssessment> getLatestLayer3AssessmentForOffender(long oasysOffenderId) {
        try {
           return Optional.ofNullable(restTemplate.getForEntity(
                   assessmentApiBasePath + "/offenders/oasysOffenderId/{oasysOffenderId}/assessments/latest?assessmentType=LAYER_3",
                   OasysAssessment.class, oasysOffenderId).getBody());
        }
        catch(HttpClientErrorException e) {
            if(e.getRawStatusCode() == 404) {
                log.info("Assessment for offender {} not found", oasysOffenderId, LogEvent.OASYS_ASSESSMENT_NOT_FOUND);
                return Optional.empty();
            }
            log.error("Failed to retrieve assessment for offender", LogEvent.OASYS_ASSESSMENT_CLIENT_FAILURE);
            throw new OasysClientException("Failed to retrieve assessment for offender");
        }
    }

    public List<OasysSentencePlan> getSentencePlansForOffender(long oasysOffenderId) {
        try {
            return restTemplate.exchange(
                    assessmentApiBasePath + "/offenders/oasysOffenderId/{oasysOffenderId}/fullSentencePlans",
                    HttpMethod.GET,null, new ParameterizedTypeReference<List<OasysSentencePlan>>(){}, oasysOffenderId).getBody();
        }
        catch(HttpClientErrorException e) {
            if(e.getRawStatusCode() == 404) {
                log.info("Sentence Plans for offender {} not found", oasysOffenderId, LogEvent.OASYS_ASSESSMENT_NOT_FOUND);
                return Collections.emptyList();
            }
            log.error("Failed to retrieve sentence plans for offender", LogEvent.OASYS_ASSESSMENT_CLIENT_FAILURE);
            throw new OasysClientException("Failed to retrieve sentence plans for offender");
        }
    }

    public List<OasysRefElement> getInterventionRefData() {
        try {
            return restTemplate.exchange(assessmentApiBasePath + "/referencedata/INTERVENTION",HttpMethod.GET, null, new ParameterizedTypeReference<List<OasysRefElement>>() {}).getBody();
        }
        catch(HttpClientErrorException e) {
            log.error("Failed to retrieve intervention reference data", LogEvent.OASYS_ASSESSMENT_CLIENT_FAILURE);
            throw new OasysClientException("Failed to retrieve intervention reference data");
        }
    }
}
