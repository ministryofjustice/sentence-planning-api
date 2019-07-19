package uk.gov.digital.justice.hmpps.sentenceplan.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;
import uk.gov.digital.justice.hmpps.sentenceplan.client.exception.OasysClientException;
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

    public Optional<OasysOffender> getOffenderById(String oasysOffenderId) {
        try {
            return Optional.ofNullable(restTemplate.getForEntity(assessmentApiBasePath + "/offenders/oasysOffenderId/{oasysOffenderId}", OasysOffender.class, oasysOffenderId).getBody());
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

    public Optional<OasysAssessment> getLatestLayer3AssessmentForOffender(String oasysOffenderId) {


        try {
           return Optional.ofNullable(restTemplate.getForEntity(assessmentApiBasePath + "/offenders/oasysOffenderId/{oasysOffenderId}/assessments/latest", OasysAssessment.class, oasysOffenderId).getBody());
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
}
