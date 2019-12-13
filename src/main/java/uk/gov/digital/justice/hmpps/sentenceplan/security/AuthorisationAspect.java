package uk.gov.digital.justice.hmpps.sentenceplan.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.digital.justice.hmpps.sentenceplan.application.RequestData;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.service.OffenderService;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;

import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class AuthorisationAspect {

    private OASYSAssessmentAPIClient oasysAssessmentAPIClient;
    private OffenderService offenderService;
    private RequestData requestData;


    public AuthorisationAspect(OASYSAssessmentAPIClient oasysAssessmentAPIClient, RequestData requestData, OffenderService offenderService) {
        this.oasysAssessmentAPIClient = oasysAssessmentAPIClient;
        this.offenderService = offenderService;
        this.requestData = requestData;
    }

    @Around("@annotation(authorised)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Authorised authorised) throws Throwable {

        Long oasysOffenderId = null;
        if (joinPoint.getArgs()[0] instanceof UUID) {
            var sentencePlanUuid = (UUID) joinPoint.getArgs()[0];
            oasysOffenderId = getSentencePlanOffenderId(sentencePlanUuid);
        } else if (joinPoint.getArgs()[0] instanceof Long) {
            oasysOffenderId = (Long) joinPoint.getArgs()[0];
        } else {
            throw new AuthorisationException("Unable parse method parameters for type " + joinPoint.getArgs()[0].getClass().getName());
        }

        var authorisationResult =  oasysAssessmentAPIClient.authoriseUserAccess(requestData.getUsername(), oasysOffenderId, authorised.accessLevel());
        if (authorisationResult) {
            return joinPoint.proceed();
        }
        throw new AuthorisationException(String.format("User %s is not authorised to access the requested resource", requestData.getUsername()));
    }

    private Long getSentencePlanOffenderId(UUID sentencePlanUuid) {
        return offenderService.getSentencePlanOffender(sentencePlanUuid).getOasysOffenderId();
    }
}