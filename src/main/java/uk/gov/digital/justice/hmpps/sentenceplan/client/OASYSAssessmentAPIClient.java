package uk.gov.digital.justice.hmpps.sentenceplan.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.*;
import uk.gov.digital.justice.hmpps.sentenceplan.client.exception.OasysClientException;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffenderPermissionResource.SENTENCE_PLAN;


@Component
@Slf4j
public class OASYSAssessmentAPIClient {

    private final RetryingOauth2RestTemplate restTemplate;
    private final String assessmentApiBasePath;

    public OASYSAssessmentAPIClient(RetryingOauth2RestTemplate restTemplate, @Value("${assessment.api.uri.root}") String assessmentApiBasePath) {
        this.assessmentApiBasePath = assessmentApiBasePath;
        this.restTemplate = restTemplate;
    }

    public Optional<OasysOffender> getOffenderById(long oasysOffenderId) {
        try {
            var url = String.format("%s/offenders/oasysOffenderId/%s", assessmentApiBasePath, oasysOffenderId);
            var response = restTemplate.get(url, OasysOffender.class);
            return Optional.ofNullable(response.getBody());
        }
        catch(HttpClientErrorException e) {
            if(e.getRawStatusCode() == 404) {
                log.info("Offender {} not found", oasysOffenderId, value(EVENT, LogEvent.OASYS_ASSESSMENT_NOT_FOUND));
                return Optional.empty();
            }
            log.error("Failed to retrieve assessment for offender", value(EVENT, LogEvent.OASYS_ASSESSMENT_CLIENT_FAILURE));
            throw new OasysClientException("Failed to retrieve assessment for offender");
        }
    }

    public Optional<OasysAssessment> getLatestLayer3AssessmentForOffender(long oasysOffenderId) {
        try {

            var url = String.format("%s/offenders/oasysOffenderId/%s/assessments/latest?assessmentType=LAYER_3", assessmentApiBasePath, oasysOffenderId);
            var response = restTemplate.get(url, OasysAssessment.class);
            return Optional.ofNullable(response.getBody());
        }
        catch(HttpClientErrorException e) {
            if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Assessment for offender {} not found", oasysOffenderId, value(EVENT, LogEvent.OASYS_ASSESSMENT_NOT_FOUND));
                return Optional.empty();
            }
            log.error("Failed to retrieve assessment for offender", value(EVENT, LogEvent.OASYS_ASSESSMENT_CLIENT_FAILURE));
            throw new OasysClientException("Failed to retrieve assessment for offender");
        }
    }

    public List<OasysSentencePlanDto> getSentencePlansForOffender(long oasysOffenderId) {
        try {
            var url = String.format("%s/offenders/oasysOffenderId/%s/fullSentencePlans", assessmentApiBasePath, oasysOffenderId);
            var response = restTemplate.get(url, new ParameterizedTypeReference<List<OasysSentencePlanDto>>(){});
            return response.getBody();
        }
        catch(HttpClientErrorException e) {
            if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Sentence Plans for offender {} not found", oasysOffenderId, value(EVENT, LogEvent.OASYS_ASSESSMENT_NOT_FOUND));
                return Collections.emptyList();
            }
            log.error("Failed to retrieve sentence plans for offender", value(EVENT, LogEvent.OASYS_ASSESSMENT_CLIENT_FAILURE));
            throw new OasysClientException("Failed to retrieve sentence plans for offender");
        }
    }

    public Optional<OasysSentencePlanDto> getSentencePlanById(long oasysOffenderId, long oasysSetId) {
        try {
            var url = String.format("%s/offenders/oasysOffenderId/%s/fullSentencePlans/%s", assessmentApiBasePath, oasysOffenderId, oasysSetId);
            var response = restTemplate.get(url, OasysSentencePlanDto.class);
            return Optional.ofNullable(response.getBody());
        }
        catch(HttpClientErrorException e) {
            if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Sentence Plan for offender {} nad Oasys Set {} not found", oasysOffenderId, oasysSetId, value(EVENT, LogEvent.OASYS_ASSESSMENT_NOT_FOUND));
                return Optional.empty();
            }
            log.error("Failed to retrieve sentence plans for offender", value(EVENT, LogEvent.OASYS_ASSESSMENT_CLIENT_FAILURE));
            throw new OasysClientException("Failed to retrieve sentence plan for offender");
        }
    }

    public List<OasysRefElement> getInterventionRefData() {
        try {
            var url = String.format("%s/referencedata/INTERVENTION", assessmentApiBasePath);
            var response = restTemplate.get(url, new ParameterizedTypeReference<List<OasysRefElement>>() {});
            return response.getBody();
        }
        catch(HttpClientErrorException e) {
            log.error("Failed to retrieve intervention reference data", value(EVENT, LogEvent.OASYS_ASSESSMENT_CLIENT_FAILURE));
            throw new OasysClientException("Failed to retrieve intervention reference data");
        }
    }

    @Cacheable("offenderAccess")
    public OasysAuthorisationDto authoriseUserAccess(String username, Long oasysOffenderId) {
        try {
             var url = String.format("%s/authentication/user/%s/offender/%s/%s", assessmentApiBasePath, username, oasysOffenderId, SENTENCE_PLAN);
             var response = restTemplate.get(url, OasysAuthorisationDto.class);
             return response.getBody();
        }
        catch(HttpClientErrorException e) {
            if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Failed to authorise User {} offender {} not found", oasysOffenderId, value(EVENT, OASYS_OFFENDER_NOT_FOUND));
                throw new EntityNotFoundException(String.format("OASys offender %s not found", oasysOffenderId));
            }
            else {
                log.error("Failed to authorise User {} for offender {}", oasysOffenderId, value(EVENT, OASYS_ASSESSMENT_CLIENT_FAILURE));
            }
            throw new OasysClientException("Failed to call OASys authorisation service");
        }
    }


}
