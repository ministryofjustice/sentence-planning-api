package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.CurrentSentencePlanForOffenderExistsException;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.*;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Collections.EMPTY_LIST;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.PlanStatus.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.StepOwner.PRACTITIONER;

@RunWith(MockitoJUnitRunner.class)
public class SentencePlanServiceTest {

    @Mock
    private SentencePlanRepository sentencePlanRepository;

    @Mock
    private OffenderService offenderService;

    @Mock
    private AssessmentService assessmentService;

    @Mock
    private MotivationRefService motivationRefService;

    private final String oasysOffenderId = "123456789";

    private SentencePlanService service;

    private final UUID sentencePlanUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private List<MotivationRefEntity> motivations;

    @Before
    public void setup() {
        motivations = List.of(new MotivationRefEntity("motivation 1", "motivation 1"),
                new MotivationRefEntity("motivation 1", "motivation 1"));
        when(motivationRefService.getAllMotivations()).thenReturn(motivations);
        service = new SentencePlanService(sentencePlanRepository, offenderService, assessmentService, motivationRefService);
    }

    @Test
    public void createSentencePlanShouldRetrieveOffenderAndAssessmentAndSavePlan() {
        var offender = mock(OffenderEntity.class);;

        when(offenderService.getOffenderByType(oasysOffenderId,  OffenderReferenceType.OASYS)).thenReturn(offender);
        when(sentencePlanRepository.findByOffenderUuid(any())).thenReturn(null);

        when(sentencePlanRepository.save(any())).thenReturn(getNewSentencePlan());

        service.createSentencePlan(oasysOffenderId, OffenderReferenceType.OASYS);

        verify(offenderService,times(1)).getOffenderByType(oasysOffenderId,  OffenderReferenceType.OASYS);
        verify(sentencePlanRepository,times(1)).save(any());
    }


    @Test
    public void shouldNotCreateSentencePlanIfCurrentPlanExistsForOffender() {
        var offender = mock(OffenderEntity.class);;

        when(offenderService.getOffenderByType(oasysOffenderId,  OffenderReferenceType.OASYS)).thenReturn(offender);
        when(sentencePlanRepository.findByOffenderUuid(any())).thenReturn(getNewSentencePlan());

        var exception = catchThrowable(() -> { service.createSentencePlan(oasysOffenderId, OffenderReferenceType.OASYS); });
        assertThat(exception).isInstanceOf(CurrentSentencePlanForOffenderExistsException.class);

        verify(offenderService,times(1)).getOffenderByType(oasysOffenderId,  OffenderReferenceType.OASYS);
        verify(sentencePlanRepository,never()).save(any());
    }

    @Test
    public void shouldAddStepToSentencePlan() {

        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getNewSentencePlan());

        var needs = List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        var steps = service.addStep(sentencePlanUuid, PRACTITIONER, null, "a strength", "a description", null, needs);

        assertThat(steps.size()).isEqualTo(1);
        var step = steps.get(0);
        assertThat(step.getDescription()).isEqualTo("a description");
        assertThat(step.getStrength()).isEqualTo("a strength");
        assertThat(step.getIntervention()).isNull();
        assertThat(step.getOwnerOther()).isNull();
        assertThat(step.getOwner()).isEqualTo(PRACTITIONER);
        assertThat(step.getNeeds()).hasSize(1);

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void shouldUpdatePlanStatusToSTARTEDWhenFirstStepIsAdded() {

        var sentencePlan =  getNewSentencePlan();

        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        var needs = List.of(UUID.randomUUID());


        assertThat(sentencePlan.getStatus()).isEqualTo(DRAFT);
        assertThat(sentencePlan.getData().getSteps()).hasSize(0);
        var steps = service.addStep(sentencePlanUuid, PRACTITIONER, null, "a strength", "a description", null, needs);

        assertThat(steps).hasSize(1);
        assertThat(sentencePlan.getStatus()).isEqualTo(STARTED);

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void shouldNotUpdatePlanStatusFirstStepIsAddedButStatusIsNotDRAFT() {

        var sentencePlan =  getNewSentencePlan();
        sentencePlan.setStatus(COMPLETE);

        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        var needs = List.of(UUID.randomUUID());

        assertThat(sentencePlan.getData().getSteps()).hasSize(0);
        var steps = service.addStep(sentencePlanUuid, PRACTITIONER, null, "a strength", "a description", null, needs);

        assertThat(steps).hasSize(1);
        assertThat(sentencePlan.getStatus()).isEqualTo(COMPLETE);

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void shouldGetStepsForSentencePlan() {

        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getSentencePlanWithSteps());
        var steps = service.getSentencePlanSteps(sentencePlanUuid);
        assertThat(steps.size()).isEqualTo(1);
        var step = steps.get(0);
        assertThat(step.getDescription()).isEqualTo("a description");
        assertThat(step.getStrength()).isEqualTo("a strength");
        assertThat(step.getIntervention()).isNull();
        assertThat(step.getOwnerOther()).isNull();
        assertThat(step.getOwner()).isEqualTo(PRACTITIONER);
        assertThat(step.getNeeds().get(0).getId()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
    }

    @Test
    public void getSentencePlanShouldRetrievePlanFromRepository() {
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getNewSentencePlan());
        service.getSentencePlanFromUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void getSentencePlanShouldReturnSentencePlanFromEntity() {
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getNewSentencePlan());
        var result =  service.getSentencePlanFromUuid(sentencePlanUuid);
        assertThat(result.getUuid()).isEqualTo(sentencePlanUuid);
        assertThat(result.getCreatedOn()).isEqualTo(LocalDateTime.of(2019,6,1, 11,00));
        assertThat(result.getStatus()).isEqualTo(DRAFT);
        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void getSentencePlanShouldThrowNotFoundException() {
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(null);
        var exception = catchThrowable(() -> { service.getSentencePlanFromUuid(sentencePlanUuid); });
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Sentence Plan " + sentencePlanUuid   + " not found");

    }

    @Test
    public void updateMotivationsShouldNotSaveToRepositoryEmpty() {
        var sentencePlan = getNewSentencePlan();

        service.updateMotivations(sentencePlanUuid, new HashMap<>());
        verify(sentencePlanRepository,times(0)).findByUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(0)).save(sentencePlan);
    }

    @Test
    public void updateMotivationsShouldSaveToRepository() {
        var sentencePlan = getNewSentencePlan();
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        service.updateMotivations(sentencePlanUuid, Map.of(UUID.randomUUID(), UUID.randomUUID()));
        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(1)).save(sentencePlan);
    }

    @Test
    public void updateStepShouldSaveToRepository() {
        var sentencePlan = getSentencePlanWithSteps();
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        StepEntity stepToUpdate = sentencePlan.getData().getSteps().stream().findFirst().get();
        service.updateStep(sentencePlanUuid, stepToUpdate.getId(), PRACTITIONER, null, "Strong", "Desc", "Inter", List.of(UUID.randomUUID()), StepStatus.COMPLETE);

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    private SentencePlanEntity getNewSentencePlan() {
        return SentencePlanEntity.builder()
                .createdOn(LocalDateTime.of(2019,6,1, 11,00))
                .status(DRAFT)
                .uuid(sentencePlanUuid)
                .needs(List.of(NeedEntity.builder().uuid(UUID.fromString("11111111-1111-1111-1111-111111111111")).description("description").motivations(EMPTY_LIST).build()))
                .data(new SentencePlanPropertiesEntity()).build();
    }

    private SentencePlanEntity getSentencePlanWithSteps() {

        var needs = List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        var steps = List.of(new StepEntity(PRACTITIONER, null, "a description", "a strength", StepStatus.NOT_IN_PROGRESS, needs, null));
        return SentencePlanEntity.builder()
                .createdOn(LocalDateTime.of(2019,6,1, 11,00))
                .status(DRAFT)
                .uuid(sentencePlanUuid)
                .needs(List.of(NeedEntity.builder().uuid(UUID.fromString("11111111-1111-1111-1111-111111111111")).description("description").motivations(EMPTY_LIST).build()))
                .data(SentencePlanPropertiesEntity.builder().steps(steps).build()).build();
    }

}