package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysSentencePlan;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.CurrentSentencePlanForOffenderExistsException;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.*;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;

import java.time.*;
import java.util.*;

import static java.util.Collections.EMPTY_LIST;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.PlanStatus.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.PRACTITIONER;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.SERVICE_USER;

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

    @Mock
    private OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    private final String oasysOffenderId = "123456789";

    private SentencePlanService service;

    private final UUID sentencePlanUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private List<MotivationRefEntity> motivations;

    Clock clock =  Clock.fixed(Instant.parse("2019-06-01T10:00:00.00Z"), ZoneId.systemDefault());

    @Before
    public void setup() {
        motivations = List.of(new MotivationRefEntity("motivation 1", "motivation 1"),
                new MotivationRefEntity("motivation 1", "motivation 1"));
        when(motivationRefService.getAllMotivations()).thenReturn(motivations);
        service = new SentencePlanService(sentencePlanRepository, offenderService, assessmentService, motivationRefService, oasysAssessmentAPIClient);
    }

    @Test
    public void createSentencePlanShouldRetrieveOffenderAndAssessmentAndSavePlan() {
        var offender = mock(OffenderEntity.class);;

        when(offenderService.getOffenderByType(oasysOffenderId,  OffenderReferenceType.OASYS)).thenReturn(offender);
        when(sentencePlanRepository.findByOffenderUuid(any())).thenReturn(null);
        when(sentencePlanRepository.findByOffenderUuid(any())).thenReturn(EMPTY_LIST);
        when(sentencePlanRepository.save(any())).thenReturn(getNewSentencePlan(sentencePlanUuid));

        service.createSentencePlan(oasysOffenderId, OffenderReferenceType.OASYS);

        verify(offenderService,times(1)).getOffenderByType(oasysOffenderId,  OffenderReferenceType.OASYS);
        verify(sentencePlanRepository,times(1)).save(any());
    }

    @Test
    public void shouldNotCreateSentencePlanIfCurrentPlanExistsForOffender() {
        var offender = mock(OffenderEntity.class);;

        when(offenderService.getOffenderByType(oasysOffenderId,  OffenderReferenceType.OASYS)).thenReturn(offender);
        when(sentencePlanRepository.findByOffenderUuid(any())).thenReturn(List.of(getNewSentencePlan(sentencePlanUuid)));  when(sentencePlanRepository.findByOffenderUuid(any())).thenReturn(List.of(getNewSentencePlan(sentencePlanUuid)));

        var exception = catchThrowable(() -> { service.createSentencePlan(oasysOffenderId, OffenderReferenceType.OASYS); });
        assertThat(exception).isInstanceOf(CurrentSentencePlanForOffenderExistsException.class);

        verify(offenderService,times(1)).getOffenderByType(oasysOffenderId,  OffenderReferenceType.OASYS);
        verify(sentencePlanRepository,never()).save(any());
    }

    @Test
    public void shouldAddActionToSentencePlan() {

        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getNewSentencePlan(sentencePlanUuid));

        var needs = List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        var actions = service.addAction(sentencePlanUuid, List.of(PRACTITIONER), null, "a strength", ActionStatus.IN_PROGRESS, "a description", null, needs);

        assertThat(actions.size()).isEqualTo(1);
        var action = actions.get(0);
        assertThat(action.getDescription()).isEqualTo("a description");
        assertThat(action.getStrength()).isEqualTo("a strength");
        assertThat(action.getIntervention()).isNull();
        assertThat(action.getOwnerOther()).isNull();
        Assertions.assertThat(action.getOwner()).hasSize(1);
        Assertions.assertThat(action.getOwner()).contains(PRACTITIONER);
        assertThat(action.getNeeds()).hasSize(1);

        //Priority should be lowest
        assertThat(action.getPriority()).isEqualTo(0);

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void shouldAddActionToSentencePlanMultipleOwner() {

        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getNewSentencePlan(sentencePlanUuid));

        var needs = List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        var actions = service.addAction(sentencePlanUuid, List.of(PRACTITIONER, SERVICE_USER), null, "a strength", ActionStatus.IN_PROGRESS, "a description", null, needs);

        assertThat(actions.size()).isEqualTo(1);
        var action = actions.get(0);
        assertThat(action.getDescription()).isEqualTo("a description");
        assertThat(action.getStrength()).isEqualTo("a strength");
        assertThat(action.getIntervention()).isNull();
        assertThat(action.getOwnerOther()).isNull();
        Assertions.assertThat(action.getOwner()).hasSize(2);
        Assertions.assertThat(action.getOwner()).contains(PRACTITIONER);
        Assertions.assertThat(action.getOwner()).contains(SERVICE_USER);
        assertThat(action.getNeeds()).hasSize(1);

        //Priority should be lowest
        assertThat(action.getPriority()).isEqualTo(0);

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void shouldAddActionToSentencePlanPriority() {

        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getNewSentencePlan(sentencePlanUuid));

        var needs = List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        var actions = service.addAction(sentencePlanUuid, List.of(PRACTITIONER), null, "a strength", ActionStatus.IN_PROGRESS,"a description", null, needs);

        assertThat(actions.size()).isEqualTo(1);
        var action = actions.get(0);
        //Priority should be lowest
        assertThat(action.getPriority()).isEqualTo(0);


        var newActions = service.addAction(sentencePlanUuid, List.of(SERVICE_USER), null, "a strength", ActionStatus.IN_PROGRESS, "a description", null, needs);

        assertThat(newActions.size()).isEqualTo(2);
        //Now the new priority should be lowest
        assertThat(newActions.get(0).getPriority()).isEqualTo(0);
        assertThat(newActions.get(0).getOwner()).hasSize(1);
        assertThat(newActions.get(0).getOwner()).contains(PRACTITIONER);

        assertThat(newActions.get(1).getPriority()).isEqualTo(1);
        assertThat(newActions.get(1).getOwner()).hasSize(1);
        assertThat(newActions.get(1).getOwner()).contains(SERVICE_USER);


        verify(sentencePlanRepository,times(2)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void shouldUpdatePlanStatusToSTARTEDWhenFirstActionIsAdded() {

        var sentencePlan =  getNewSentencePlan(sentencePlanUuid);

        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        var needs = List.of(UUID.randomUUID());


        assertThat(sentencePlan.getStatus()).isEqualTo(DRAFT);
        assertThat(sentencePlan.getData().getActions()).hasSize(0);
        var actions = service.addAction(sentencePlanUuid, List.of(PRACTITIONER), null, "a strength", ActionStatus.IN_PROGRESS,"a description", null, needs);

        assertThat(actions).hasSize(1);
        assertThat(sentencePlan.getStatus()).isEqualTo(STARTED);

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void shouldNotUpdatePlanStatusFirstActionIsAddedButStatusIsNotDRAFT() {

        var sentencePlan =  getNewSentencePlan(sentencePlanUuid);
        sentencePlan.setStatus(COMPLETE);

        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        var needs = List.of(UUID.randomUUID());

        assertThat(sentencePlan.getData().getActions()).hasSize(0);
        var actions = service.addAction(sentencePlanUuid, List.of(PRACTITIONER), null, "a strength", ActionStatus.IN_PROGRESS, "a description", null, needs);

        assertThat(actions).hasSize(1);
        assertThat(sentencePlan.getStatus()).isEqualTo(COMPLETE);

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void shouldGetActionsForSentencePlan() {

        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getSentencePlanWithOneAction());
        var actions = service.getSentencePlanActions(sentencePlanUuid);
        assertThat(actions.size()).isEqualTo(1);
        var action = actions.get(0);
        assertThat(action.getDescription()).isEqualTo("a description");
        assertThat(action.getStrength()).isEqualTo("a strength");
        assertThat(action.getIntervention()).isNull();
        assertThat(action.getOwnerOther()).isNull();
        Assertions.assertThat(action.getOwner()).hasSize(1);
        Assertions.assertThat(action.getOwner()).contains(PRACTITIONER);
        assertThat(action.getNeeds().get(0).getId()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
    }

    @Test
    public void getSentencePlanShouldRetrievePlanFromRepository() {
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getNewSentencePlan(sentencePlanUuid));
        service.getSentencePlanFromUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void getSentencePlanShouldReturnSentencePlanFromEntity() {
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getNewSentencePlan(sentencePlanUuid));
        var result =  service.getSentencePlanFromUuid(sentencePlanUuid);
        assertThat(result.getUuid()).isEqualTo(sentencePlanUuid);
        assertThat(result.getCreatedDate()).isEqualTo(LocalDateTime.of(2019,6,1, 11,00));
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
        var sentencePlan = getNewSentencePlan(sentencePlanUuid);

        service.updateMotivations(sentencePlanUuid, new HashMap<>());
        verify(sentencePlanRepository,times(0)).findByUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(0)).save(sentencePlan);
    }

    @Test
    public void updateMotivationsShouldSaveToRepository() {
        var sentencePlan = getNewSentencePlan(sentencePlanUuid);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        service.updateMotivations(sentencePlanUuid, Map.of(UUID.randomUUID(), UUID.randomUUID()));
        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(1)).save(sentencePlan);
    }

    @Test
    public void updateActionPriorityShouldNotSaveToRepositoryEmpty() {
        var sentencePlan = getNewSentencePlan(sentencePlanUuid);

        service.updateActionPriorities(sentencePlanUuid, new HashMap<>());
        verify(sentencePlanRepository,times(0)).findByUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(0)).save(sentencePlan);

    }

    @Test
    public void updateActionShouldSaveToRepository() {
        var sentencePlan = getSentencePlanWithOneAction();
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        var actionToUpdate = sentencePlan.getData().getActions().stream().findFirst().get();
        service.updateAction(sentencePlanUuid, actionToUpdate.getId(), List.of(PRACTITIONER), null, "Strong", "Desc", "Inter", List.of(UUID.randomUUID()), ActionStatus.COMPLETED);

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(1)).save(sentencePlan);
    }

    @Test
    public void progressActionShouldSaveToRepository() {
        var sentencePlan = getSentencePlanWithOneAction();
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        var actionToProgress = sentencePlan.getData().getActions().stream().findFirst().get();
        service.progressAction(sentencePlanUuid, actionToProgress.getId(), ActionStatus.ABANDONED);

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(1)).save(sentencePlan);
    }

    @Test
    public void getSentencePlansForOffenderShouldReturnOASysPlans() {

        var offender = new OffenderEntity(1L, UUID.fromString("11111111-1111-1111-1111-111111111111"), 12345L, null,null, 123L, LocalDateTime.now(), EMPTY_LIST);
        var legacyPlan =  OasysSentencePlan.builder()
                .completedDate(LocalDate.of(2019,1,1))
                .createdDate(LocalDate.of(2018,1,1))
                .oasysSetId(123456L).build();


        when(oasysAssessmentAPIClient.getSentencePlansForOffender(12345L)).thenReturn(List.of(legacyPlan));
        when(offenderService.getOffenderByType("12345", OffenderReferenceType.OASYS)).thenReturn(offender);
        when(sentencePlanRepository.findByOffenderUuid(offender.getUuid())).thenReturn(EMPTY_LIST);

        var result = service.getSentencePlansForOffender(12345L).get(0);

        assertThat(result.getPlanId()).isEqualTo("123456");
        assertThat(result.getCreatedDate()).isEqualTo(LocalDate.of(2018,1,1));
        assertThat(result.getCompletedDate()).isEqualTo (LocalDate.of(2019,1,1));
        verify(oasysAssessmentAPIClient, times(1)).getSentencePlansForOffender(12345L);

    }

    @Test
    public void getSentencePlansForOffenderShouldReturnNewPlans() {

        var offender = new OffenderEntity(1L, UUID.fromString("11111111-1111-1111-1111-111111111111"), 12345L, null,null,123L, LocalDateTime.now(), EMPTY_LIST);
        when(oasysAssessmentAPIClient.getSentencePlansForOffender(12345L)).thenReturn(EMPTY_LIST);
        when(offenderService.getOffenderByType("12345", OffenderReferenceType.OASYS)).thenReturn(offender);
        when(sentencePlanRepository.findByOffenderUuid(offender.getUuid())).thenReturn(List.of(getNewSentencePlan(sentencePlanUuid)));

        var result = service.getSentencePlansForOffender(12345L).get(0);

        assertThat(result.getPlanId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(result.getCreatedDate()).isEqualTo(LocalDate.of(2019,6,1));
        assertThat(result.getCompletedDate()).isNull();
        verify(sentencePlanRepository, times(1)).findByOffenderUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    }

    @Test
    public void getSentencePlansForOffenderShouldOrderPlansByCreatedDate() {

        var offender = new OffenderEntity(1L, UUID.fromString("11111111-1111-1111-1111-111111111111"), 12345L, null,null,123L, LocalDateTime.now(), EMPTY_LIST);
        var legacyPlan =  OasysSentencePlan.builder()
                .completedDate(LocalDate.of(2019,1,1))
                .createdDate(LocalDate.of(2018,1,1))
                .oasysSetId(123456L).build();
        when(oasysAssessmentAPIClient.getSentencePlansForOffender(12345L)).thenReturn(List.of(legacyPlan));
        when(offenderService.getOffenderByType("12345", OffenderReferenceType.OASYS)).thenReturn(offender);

        var sentencePlan1 = getNewSentencePlan(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        var sentencePlan2 = getNewSentencePlan(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        var sentencePlan3 = getNewSentencePlan(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        sentencePlan1.setCreatedDate(LocalDateTime.of(2019,1,2,1,0));
        sentencePlan2.setCreatedDate(LocalDateTime.of(2019,1,1,1,0));
        sentencePlan3.setCreatedDate(LocalDateTime.of(2019,1,3,1,0));

        when(sentencePlanRepository.findByOffenderUuid(offender.getUuid())).thenReturn(List.of(sentencePlan1,sentencePlan2,sentencePlan3));

        var result = service.getSentencePlansForOffender(12345L);
        assertThat(result).hasSize(4);

        assertThat(result.get(3).getPlanId()).isEqualTo("123456");
        assertThat(result.get(2).getPlanId()).isEqualTo("22222222-2222-2222-2222-222222222222");
        assertThat(result.get(1).getPlanId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(result.get(0).getPlanId()).isEqualTo("33333333-3333-3333-3333-333333333333");

    }

    @Test
    public void addCommentsShouldSaveToRepository() {
        var sentencePlan = getSentencePlanWithOneAction();
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        var comment = new AddCommentRequest("Any Comment", CommentType.DIVERSITY);

        service.addSentencePlanComments(sentencePlanUuid, List.of(comment));

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(1)).save(sentencePlan);
    }

    @Test
    public void addNoCommentsShouldNotSaveToRepository() {
        var sentencePlan = getSentencePlanWithOneAction();

        service.addSentencePlanComments(sentencePlanUuid, List.of());

        verify(sentencePlanRepository,times(0)).findByUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(0)).save(sentencePlan);
    }

    @Test
    public void addMultipleCommentsShouldSaveToRepositoryOnce() {

        var sentencePlan = getSentencePlanWithOneAction();
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        var comment1 = new AddCommentRequest("Any Comment", CommentType.ABOUTME);
        var comment2 = new AddCommentRequest("Any Other Comment", CommentType.DECISIONS);


        service.addSentencePlanComments(sentencePlanUuid, List.of(comment1, comment2));

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(1)).save(sentencePlan);
    }

    public void getLegacySentencePlanShouldReturnPlanForId() {

        var offender = new OffenderEntity(1L, UUID.fromString("11111111-1111-1111-1111-111111111111"), 12345L, null,null,123L, LocalDateTime.now(), EMPTY_LIST);
        var legacyPlan1 =  OasysSentencePlan.builder()
                .completedDate(LocalDate.of(2019,1,1))
                .createdDate(LocalDate.of(2018,1,1))
                .oasysSetId(1L).build();

        var legacyPlan2 =  OasysSentencePlan.builder()
                .completedDate(LocalDate.of(2018,1,1))
                .createdDate(LocalDate.of(2017,1,1))
                .oasysSetId(2L).build();

        when(oasysAssessmentAPIClient.getSentencePlansForOffender(12345L)).thenReturn(List.of(legacyPlan1, legacyPlan2));

        var result = service.getLegacySentencePlan(12345L, "1");

        assertThat(result.getOasysSetId()).isEqualTo(1L);
        assertThat(result.getCompletedDate()).isEqualTo(LocalDate.of(2019,1,1));
        assertThat(result.getCreatedDate()).isEqualTo(LocalDate.of(2018,1,1));
    }

    private SentencePlanEntity getNewSentencePlan(UUID uuid) {

        return SentencePlanEntity.builder()
                .createdDate(LocalDateTime.of(2019,6,1, 11,00))
                .status(DRAFT)
                .uuid(uuid)
                .needs(List.of(NeedEntity.builder().uuid(UUID.fromString("11111111-1111-1111-1111-111111111111")).description("description").motivations(EMPTY_LIST).build()))
                .data(new SentencePlanPropertiesEntity()).build();
    }

    private SentencePlanEntity getSentencePlanWithOneAction() {

        var needs = List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        var sentencePlanProperty = new SentencePlanPropertiesEntity();
        sentencePlanProperty.addActions(new ActionEntity(List.of(PRACTITIONER), null, "a description", "a strength", ActionStatus.PAUSED, needs, null));
        return SentencePlanEntity.builder()
                .createdDate(LocalDateTime.of(2019,6,1, 11,00))
                .status(DRAFT)
                .uuid(sentencePlanUuid)
                .needs(List.of(NeedEntity.builder().uuid(UUID.fromString("11111111-1111-1111-1111-111111111111")).description("description").motivations(EMPTY_LIST).build()))
                .data(sentencePlanProperty).build();
    }

    private SentencePlanEntity getSentencePlanWithMultipleActions() {
        var needs = List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        var newAction = new ActionEntity(List.of(PRACTITIONER), null, "two description", "two strength", ActionStatus.IN_PROGRESS, needs, null);
        var sentencePlan = getSentencePlanWithOneAction();
        sentencePlan.addAction(newAction);
        return sentencePlan;
    }



}