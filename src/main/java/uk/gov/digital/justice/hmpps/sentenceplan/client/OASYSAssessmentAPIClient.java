package uk.gov.digital.justice.hmpps.sentenceplan.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.*;
import uk.gov.digital.justice.hmpps.sentenceplan.client.exception.OasysClientException;
import uk.gov.digital.justice.hmpps.sentenceplan.security.AccessLevel;
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

    private final OAuth2RestTemplate restTemplate;
    private final String assessmentApiBasePath;

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
                log.info("Offender {} not found", oasysOffenderId, value(EVENT, LogEvent.OASYS_ASSESSMENT_NOT_FOUND));
                return Optional.empty();
            }
            log.error("Failed to retrieve assessment for offender", value(EVENT, LogEvent.OASYS_ASSESSMENT_CLIENT_FAILURE));
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
            return restTemplate.exchange(
                    assessmentApiBasePath + "/offenders/oasysOffenderId/{oasysOffenderId}/fullSentencePlans",
                    HttpMethod.GET,null, new ParameterizedTypeReference<List<OasysSentencePlanDto>>(){}, oasysOffenderId).getBody();
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
            return Optional.ofNullable(restTemplate.exchange(
                    assessmentApiBasePath + "/offenders/oasysOffenderId/{oasysOffenderId}/fullSentencePlans/{oasysSetId}",
                    HttpMethod.GET,null, OasysSentencePlanDto.class, oasysOffenderId, oasysSetId).getBody());
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
            return restTemplate.exchange(assessmentApiBasePath + "/referencedata/INTERVENTION",HttpMethod.GET, null, new ParameterizedTypeReference<List<OasysRefElement>>() {}).getBody();
        }
        catch(HttpClientErrorException e) {
            log.error("Failed to retrieve intervention reference data", value(EVENT, LogEvent.OASYS_ASSESSMENT_CLIENT_FAILURE));
            throw new OasysClientException("Failed to retrieve intervention reference data");
        }
    }

    @Cacheable("offenderAccess")
    public OasysAuthorisationDto authoriseUserAccess(String username, Long oasysOffenderId, Long sessionId) {
        try {
             var response = restTemplate.getForEntity(assessmentApiBasePath + "/authentication/user/{oasysUserId}/offender/{offenderId}/{resource}?sessionId={sessionId}",
                    OasysAuthorisationDto.class, username, oasysOffenderId, SENTENCE_PLAN, sessionId);
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
