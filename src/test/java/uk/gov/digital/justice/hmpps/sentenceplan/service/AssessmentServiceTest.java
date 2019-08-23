package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.AssessmentNeed;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.NoOffenderAssessmentException;


import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class AssessmentServiceTest {

    @Mock
    OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    AssessmentService assessmentService;

    SentencePlanEntity sentencePlanEntity;

    Clock clock =  Clock.fixed(Instant.parse("2019-06-01T10:00:00.00Z"), ZoneId.systemDefault());

    @Before
    public void setup() {
        assessmentService = new AssessmentService(oasysAssessmentAPIClient, clock);
        sentencePlanEntity = new SentencePlanEntity(new OffenderEntity(123456L, "123456"));
    }

    @Test
    public void shouldAddAssessmentNeedsToSentencePlan() {
        var needs = List.of(new AssessmentNeed("Accommodation",true,true,true,true),
                new AssessmentNeed("Alcohol",true,true,true,true));
        var oasysAssessment = new OasysAssessment(123456,"ACTIVE", needs,true, true);

        when(oasysAssessmentAPIClient.getLatestLayer3AssessmentForOffender(123456L))
                .thenReturn(Optional.ofNullable(oasysAssessment));
        assertThat(sentencePlanEntity.getNeeds()).isEmpty();
        assessmentService.addLatestAssessmentNeedsToPlan(sentencePlanEntity);
        assertThat(sentencePlanEntity.getNeeds().size()).isEqualTo(2);
    }

    @Test
    public void shouldNotAddNewAssessmentNeedsToSentencePlanWithin10MinutesOfLastUpdate() {
        sentencePlanEntity.setAssessmentNeedsLastImportedOn(LocalDateTime.now());
        assertThat(sentencePlanEntity.getNeeds()).isEmpty();
        assessmentService.addLatestAssessmentNeedsToPlan(sentencePlanEntity);
        verify(oasysAssessmentAPIClient, never()).getLatestLayer3AssessmentForOffender(123456L);
        assertThat(sentencePlanEntity.getNeeds()).isEmpty();
    }

    @Test
    public void shouldAddNewAssessmentNeedsToSentencePlanIfNotUpdatedInPast10Minutes() {
        var needs = List.of(new AssessmentNeed("Accommodation",true,true,true,true),
                new AssessmentNeed("Alcohol",true,true,true,true));
        var oasysAssessment = new OasysAssessment(123456,"ACTIVE", needs,true, true);

        when(oasysAssessmentAPIClient.getLatestLayer3AssessmentForOffender(123456L))
                .thenReturn(Optional.ofNullable(oasysAssessment));
        sentencePlanEntity.setAssessmentNeedsLastImportedOn(LocalDateTime.now(clock).minusMinutes(11));

        assertThat(sentencePlanEntity.getNeeds()).isEmpty();
        assessmentService.addLatestAssessmentNeedsToPlan(sentencePlanEntity);
        verify(oasysAssessmentAPIClient, times(1)).getLatestLayer3AssessmentForOffender(123456L);
    }

    @Test
    public void shouldThrowExceptionWhenNoAssessmentExistsForOffender() {
        when(oasysAssessmentAPIClient.getLatestLayer3AssessmentForOffender(123456L))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> {  assessmentService.addLatestAssessmentNeedsToPlan(sentencePlanEntity);})
                .isInstanceOf(NoOffenderAssessmentException.class);
    }

}