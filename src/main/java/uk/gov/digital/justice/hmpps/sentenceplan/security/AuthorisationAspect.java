package uk.gov.digital.justice.hmpps.sentenceplan.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import uk.gov.digital.justice.hmpps.sentenceplan.application.RequestData;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAuthorisationDto;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffenderPermissionLevel;
import uk.gov.digital.justice.hmpps.sentenceplan.service.OffenderService;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
@Slf4j
@Conditional(value = {ToggleAuthorisation.class})
public class AuthorisationAspect {

    private OASYSAssessmentAPIClient oasysAssessmentAPIClient;
    private final OffenderService offenderService;
    private final RequestData requestData;


    public AuthorisationAspect(OASYSAssessmentAPIClient oasysAssessmentAPIClient, RequestData requestData, OffenderService offenderService) {
        this.oasysAssessmentAPIClient = oasysAssessmentAPIClient;
        this.offenderService = offenderService;
        this.requestData = requestData;
    }

    @Around("@annotation(authorised)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Authorised authorised) throws Throwable {

        Long oasysOffenderId;
        if (joinPoint.getArgs()[0] instanceof UUID) {
            var sentencePlanUuid = (UUID) joinPoint.getArgs()[0];
            oasysOffenderId = getSentencePlanOffenderId(sentencePlanUuid);
        } else if (joinPoint.getArgs()[0] instanceof Long) {
            oasysOffenderId = (Long) joinPoint.getArgs()[0];
        } else {
            throw new AuthorisationException("Unable parse method parameters for type " + joinPoint.getArgs()[0].getClass().getName());
        }

        var sessionId = Optional.ofNullable(requestData.getSessionId()).map(s-> Long.valueOf(s)).orElse(null);

        var authorisationResult =  oasysAssessmentAPIClient.authoriseUserAccess(requestData.getUsername(), oasysOffenderId, sessionId);

        if (isAuthorised(authorisationResult, authorised.accessLevel())) {
            log.info("User {} authorised to perform {} on offender {}", requestData.getUsername(), joinPoint.getSignature().getName(), oasysOffenderId);
            return joinPoint.proceed();
        }
        log.warn("User {} NOT authorised to to perform {} on offender {}", requestData.getUsername(), joinPoint.getSignature().getName(), oasysOffenderId);
        throw new AuthorisationException(String.format("User %s is not authorised to access the requested resource", requestData.getUsername()));
    }

    private boolean isAuthorised(OasysAuthorisationDto authorisationResult, AccessLevel accessLevel) {
        if(authorisationResult.getOasysOffenderPermissionLevel().getAccessLevel() >= accessLevel.getLevel()) {
            return true;
        }
        return false;
    }

    private Long getSentencePlanOffenderId(UUID sentencePlanUuid) {
        return offenderService.getSentencePlanOffender(sentencePlanUuid).getOasysOffenderId();
    }
}