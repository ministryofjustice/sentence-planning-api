package uk.gov.digital.justice.hmpps.sentenceplan.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.application.RequestData;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAuthorisationDto;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffenderPermissionLevel;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffenderPermissionResource;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.service.OffenderService;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;

import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffenderPermissionLevel.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffenderPermissionResource.SENTENCE_PLAN;
import static uk.gov.digital.justice.hmpps.sentenceplan.security.AccessLevel.READ_SENTENCE_PLAN;
import static uk.gov.digital.justice.hmpps.sentenceplan.security.AccessLevel.WRITE_SENTENCE_PLAN;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationAspectTest {

    @Mock
    OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    @Mock
    OffenderService offenderService;

    @Mock
    Authorised annotation;

    @Mock
    RequestData requestData;

    @Mock( answer = Answers.RETURNS_DEEP_STUBS)
    ProceedingJoinPoint proceedingJoinPoint;

    AuthorisationAspect aspect;

    final UUID sentencePlanUuid =  UUID.fromString("11111111-1111-1111-1111-111111111111");

    final OffenderEntity offender = new OffenderEntity(123456L, "12345", null, null);

    @Before
    public void setup() {
        when(requestData.getUsername()).thenReturn("USER");

        aspect = new AuthorisationAspect(oasysAssessmentAPIClient, requestData, offenderService);

    }

    @Test
    public void shouldProceedIfUserIsAuthorisedWithSentencePlanUuid() throws Throwable {
        var authorisationDto = new OasysAuthorisationDto("USER", offender.getOasysOffenderId(), READ_ONLY, SENTENCE_PLAN);
        var args = new Object[1];
        args[0] = sentencePlanUuid;

        when(requestData.getSessionId()).thenReturn("123456");
        when(offenderService.getSentencePlanOffender(sentencePlanUuid)).thenReturn(offender);
        when(oasysAssessmentAPIClient.authoriseUserAccess("USER", offender.getOasysOffenderId(), 123456L)).thenReturn(authorisationDto);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(READ_SENTENCE_PLAN);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(offenderService, times(1)).getSentencePlanOffender(sentencePlanUuid);
    }

    @Test
    public void shouldNotProceedIfUserIsUnauthorisedWithSentencePlanUuid() throws Throwable {
        var authorisationDto = new OasysAuthorisationDto("USER", offender.getOasysOffenderId(), UNAUTHORISED, SENTENCE_PLAN);
        var args = new Object[1];
        args[0] = sentencePlanUuid;

        when(requestData.getSessionId()).thenReturn("123456");
        when(offenderService.getSentencePlanOffender(sentencePlanUuid)).thenReturn(offender);
        when(oasysAssessmentAPIClient.authoriseUserAccess("USER", offender.getOasysOffenderId(), 123456L)).thenReturn(authorisationDto);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(READ_SENTENCE_PLAN);

        assertThatThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint,annotation))
                .isInstanceOf(AuthorisationException.class)
                .hasMessageContaining("User USER is not authorised to access the requested resource");

        verify(proceedingJoinPoint, never()).proceed();
        verify(offenderService, times(1)).getSentencePlanOffender(sentencePlanUuid);
    }

    @Test
    public void shouldProceedIfUserIsAuthorisedWithOffenderId() throws Throwable {
        var authorisationDto = new OasysAuthorisationDto("USER", offender.getOasysOffenderId(), READ_ONLY, SENTENCE_PLAN);
        var args = new Object[1];
        args[0] = offender.getOasysOffenderId();

        when(requestData.getSessionId()).thenReturn("123456");
        when(oasysAssessmentAPIClient.authoriseUserAccess("USER", offender.getOasysOffenderId(), 123456L)).thenReturn(authorisationDto);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.getSignature().getName()).thenReturn("methodName");
        when(annotation.accessLevel()).thenReturn(READ_SENTENCE_PLAN);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(offenderService, never()).getSentencePlanOffender(sentencePlanUuid);
    }

    @Test
    public void shouldNotProceedIfUserIsUnauthorisedWithOffenderId() throws Throwable {
        var authorisationDto = new OasysAuthorisationDto("USER", offender.getOasysOffenderId(), UNAUTHORISED, SENTENCE_PLAN);
        var args = new Object[1];
        args[0] = offender.getOasysOffenderId();

        when(requestData.getSessionId()).thenReturn("123456");
        when(oasysAssessmentAPIClient.authoriseUserAccess("USER", offender.getOasysOffenderId(), 123456L)).thenReturn(authorisationDto);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.getSignature().getName()).thenReturn("methodName");
        when(annotation.accessLevel()).thenReturn(READ_SENTENCE_PLAN);

        assertThatThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint,annotation))
                .isInstanceOf(AuthorisationException.class)
        .hasMessageContaining("User USER is not authorised to access the requested resource");

        verify(proceedingJoinPoint, never()).proceed();
        verify(offenderService, never()).getSentencePlanOffender(sentencePlanUuid);
    }

    @Test
    public void shouldProceedIfUserIsWriteAuthorisedWithOffenderIdW() throws Throwable {
        var authorisationDto = new OasysAuthorisationDto("USER", offender.getOasysOffenderId(), WRITE, SENTENCE_PLAN);
        var args = new Object[1];
        args[0] = offender.getOasysOffenderId();

        when(requestData.getSessionId()).thenReturn("123456");
        when(oasysAssessmentAPIClient.authoriseUserAccess("USER", offender.getOasysOffenderId(), 123456L)).thenReturn(authorisationDto);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.getSignature().getName()).thenReturn("methodName");
        when(annotation.accessLevel()).thenReturn(WRITE_SENTENCE_PLAN);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(offenderService, never()).getSentencePlanOffender(sentencePlanUuid);
    }

    @Test
    public void shouldNotProceedWhenAccessLevelIsWriteAndUserHasReadAccess() throws Throwable {
        var authorisationDto = new OasysAuthorisationDto("USER", offender.getOasysOffenderId(), READ_ONLY, SENTENCE_PLAN);
        var args = new Object[1];
        args[0] = offender.getOasysOffenderId();

        when(requestData.getSessionId()).thenReturn("123456");
        when(oasysAssessmentAPIClient.authoriseUserAccess("USER", offender.getOasysOffenderId(), 123456L)).thenReturn(authorisationDto);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.getSignature().getName()).thenReturn("methodName");
        when(annotation.accessLevel()).thenReturn(WRITE_SENTENCE_PLAN);

        assertThatThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint,annotation))
                .isInstanceOf(AuthorisationException.class);
        verify(proceedingJoinPoint, never()).proceed();
        verify(offenderService, never()).getSentencePlanOffender(sentencePlanUuid);
    }

    @Test
    public void shouldNotProceedIfArgsDoNotContainUUIDOrLong() throws Throwable {
        var args = new Object[1];
        args[0] = "TEST";
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.getSignature().getName()).thenReturn("methodName");
        assertThatThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint,annotation))
                .isInstanceOf(AuthorisationException.class)
                .hasMessageContaining("Unable parse method parameters for type java.lang.String");

        verify(proceedingJoinPoint, never()).proceed();
        verify(offenderService, never()).getSentencePlanOffender(sentencePlanUuid);
    }

    @Test
    public void shouldNotProceedIfSentencePlanNotFound() throws Throwable {
        var args = new Object[1];
        args[0] = sentencePlanUuid;
        when(offenderService.getSentencePlanOffender(sentencePlanUuid)).thenThrow(new EntityNotFoundException(""));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.getSignature().getName()).thenReturn("methodName");
        assertThatThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint,annotation))
                .isInstanceOf(EntityNotFoundException.class);
        verify(proceedingJoinPoint, never()).proceed();
        verify(offenderService, times(1)).getSentencePlanOffender(sentencePlanUuid);
    }

}