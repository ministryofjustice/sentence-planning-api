package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.application.RequestData;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysSentencePlanDto;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.BusinessRuleViolationException;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.CurrentSentencePlanForOffenderExistsException;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.*;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;
import java.time.*;
import java.util.*;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.PRACTITIONER;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.SERVICE_USER;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.CommentType.LIAISON_ARRANGEMENTS;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.CommentType.YOUR_SUMMARY;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ObjectiveStatus.CLOSED;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ObjectiveStatus.OPEN;
import static uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent.*;

@RunWith(MockitoJUnitRunner.class)
public class SentencePlanServiceTest {

    @Mock
    private SentencePlanRepository sentencePlanRepository;

    @Mock
    private OffenderService offenderService;

    @Mock
    private AssessmentService assessmentService;

    @Mock
    private TimelineService timelineService;

    @Mock
    private OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    @Mock
    private RequestData requestData;

    private final Long oasysOffenderId = 123456789L;

    private SentencePlanService service;

    private final UUID sentencePlanUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private List<MotivationRefEntity> motivations;

    @Before
    public void setup() {
        motivations = List.of(new MotivationRefEntity("motivation 1"),
                new MotivationRefEntity("motivation 1"));
        service = new SentencePlanService(sentencePlanRepository, offenderService, assessmentService, timelineService, oasysAssessmentAPIClient, requestData);
        when(requestData.getUsername()).thenReturn("a user");
    }

    @Test
    public void createSentencePlanShouldRetrieveOffenderAndAssessmentAndSavePlan() {
        var offender = mock(OffenderEntity.class);

        when(offenderService.getOffenderByType(oasysOffenderId)).thenReturn(offender);
        when(sentencePlanRepository.findByOffenderUuid(any())).thenReturn(null);
        when(sentencePlanRepository.findByOffenderUuid(any())).thenReturn(Collections.emptyList());
        when(sentencePlanRepository.save(any())).thenReturn(getNewSentencePlan(sentencePlanUuid));

        service.createSentencePlan(oasysOffenderId);

        verify(offenderService,times(1)).getOffenderByType(oasysOffenderId);
        verify(sentencePlanRepository,times(1)).save(any());
        verify(timelineService,times(1)).createTimelineEntry(any(UUID.class), eq(SENTENCE_PLAN_CREATED));
    }

    @Test
    public void shouldNotCreateSentencePlanIfCurrentPlanExistsForOffender() {
        var offender = mock(OffenderEntity.class);

        when(offenderService.getOffenderByType(oasysOffenderId)).thenReturn(offender);
        when(sentencePlanRepository.findByOffenderUuid(any())).thenReturn(List.of(getNewSentencePlan(sentencePlanUuid)));  when(sentencePlanRepository.findByOffenderUuid(any())).thenReturn(List.of(getNewSentencePlan(sentencePlanUuid)));

        var exception = catchThrowable(() -> service.createSentencePlan(oasysOffenderId));
        assertThat(exception).isInstanceOf(CurrentSentencePlanForOffenderExistsException.class);

        verify(offenderService,times(1)).getOffenderByType(oasysOffenderId);
        verify(sentencePlanRepository,never()).save(any());
        verifyNoInteractions(timelineService);
    }

    @Test
    public void getSentencePlanShouldRetrievePlanFromRepository() {
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getNewSentencePlan(sentencePlanUuid));
        service.getSentencePlanFromUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }


    @Test
    public void getSentencePlanShouldThrowNotFoundException() {
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(null);
        var exception = catchThrowable(() -> service.getSentencePlanFromUuid(sentencePlanUuid));
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Sentence Plan " + sentencePlanUuid   + " not found");

    }

    @Test
    public void shouldAddObjectiveToSentencePlan() {
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);
        var needs = List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        var request = new AddSentencePlanObjectiveRequest("Objective 1", needs, true);
        var objective = service.addObjective(sentencePlanUuid, request);

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
        verify(newSentencePlan,times(1)).addObjective(any());
        verify(timelineService, times(1)).createTimelineEntry(eq(sentencePlanUuid), eq(SENTENCE_PLAN_OBJECTIVE_CREATED), any(ObjectiveEntity.class));

        assertThat(objective.getActions()).isEmpty();
        assertThat(objective.getDescription()).isEqualTo("Objective 1");
        assertThat(objective.getNeeds()).containsExactlyElementsOf(needs);
        assertThat(objective.isMeetsChildSafeguarding()).isEqualTo(true);

    }

    @Test
    public void shouldSetINPROGRESSActionStatusToABANDONEDWhenObjectiveClosed() {

        var objective1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1 = new ActionEntity(action1UUID,"Action 1", YearMonth.of(2020,11),null, List.of(PRACTITIONER),null, IN_PROGRESS);
        var objective1 = new ObjectiveEntity(objective1UUID, "Objective 1", emptyList(), Map.of(action1UUID, action1),false, OPEN, 1, LocalDateTime.now(), new ArrayList<>());
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjective(objective1UUID)).thenReturn(objective1);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);
        service.closeObjective(sentencePlanUuid, objective1UUID, "a Comment");
        assertThat(action1.getStatus()).isEqualTo(ABANDONED);
    }

    @Test
    public void shouldSetPAUSEDActionStatusToABANDONEDWhenObjectiveClosed() {

        var objective1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1 = new ActionEntity(action1UUID,"Action 1", YearMonth.of(2020,11),null, List.of(PRACTITIONER),null, PAUSED);
        var objective1 = new ObjectiveEntity(objective1UUID, "Objective 1", emptyList(), Map.of(action1UUID, action1),false, OPEN, 1, LocalDateTime.now(), new ArrayList<>());
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjective(objective1UUID)).thenReturn(objective1);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);

        service.closeObjective(sentencePlanUuid, objective1UUID, "a Comment");

        verify(timelineService, times(1)).createTimelineEntry(eq(sentencePlanUuid), eq(SENTENCE_PLAN_OBJECTIVE_CLOSED), eq(objective1));
    }

    @Test
    public void shouldSetNOTSTARTEDActionStatusToABANDONEDWhenObjectiveClosed() {

        var objective1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1 = new ActionEntity(action1UUID,"Action 1", YearMonth.of(2020,11),null, List.of(PRACTITIONER),null, NOT_STARTED);
        var objective1 = new ObjectiveEntity(objective1UUID, "Objective 1", emptyList(), Map.of(action1UUID, action1),false, OPEN, 1, LocalDateTime.now(), new ArrayList<>());
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjective(objective1UUID)).thenReturn(objective1);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);
        service.closeObjective(sentencePlanUuid, objective1UUID, "a Comment");
        assertThat(action1.getStatus()).isEqualTo(ABANDONED);
    }

    @Test
    public void shouldCreateEventWhenObjectiveIsClosed() {

        var objective1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1 = new ActionEntity(action1UUID,"Action 1", YearMonth.of(2020,11),null, List.of(PRACTITIONER),null, NOT_STARTED);
        var objective1 = new ObjectiveEntity(objective1UUID, "Objective 1", emptyList(), Map.of(action1UUID, action1),false, OPEN, 1, LocalDateTime.now(), new ArrayList<>());
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjective(objective1UUID)).thenReturn(objective1);
        when(requestData.getUsername()).thenReturn("A user");
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);
        service.closeObjective(sentencePlanUuid, objective1UUID, "a Comment");
        verify(timelineService, times(1)).createTimelineEntry(eq(sentencePlanUuid), eq(SENTENCE_PLAN_OBJECTIVE_CLOSED), any(ObjectiveEntity.class));
    }

    @Test
    public void shouldNotCreateEventWhenObjectiveIsAlreadyClosed() {

        var objective1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1 = new ActionEntity(action1UUID,"Action 1", YearMonth.of(2020,11),null, List.of(PRACTITIONER),null, NOT_STARTED);
        var objective1 = new ObjectiveEntity(objective1UUID, "Objective 1", emptyList(), Map.of(action1UUID, action1),false, CLOSED, 1, LocalDateTime.now(), new ArrayList<>());
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjective(objective1UUID)).thenReturn(objective1);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);
        service.closeObjective(sentencePlanUuid, objective1UUID, "a Comment");
        verify(timelineService, never()).createTimelineEntry(eq(sentencePlanUuid), eq(SENTENCE_PLAN_OBJECTIVE_CLOSED), any(ObjectiveEntity.class));
    }

    @Test
    public void shouldCreateStatusChangeWhenObjectiveIsClosed() {

        var objective1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1 = new ActionEntity(action1UUID,"Action 1", YearMonth.of(2020,11),null, List.of(PRACTITIONER),null, NOT_STARTED);
        var objective1 = new ObjectiveEntity(objective1UUID, "Objective 1", emptyList(), Map.of(action1UUID, action1),false, OPEN, 1, LocalDateTime.now(), new ArrayList<>());
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjective(objective1UUID)).thenReturn(objective1);
        when(requestData.getUsername()).thenReturn("A user");
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);
        service.closeObjective(sentencePlanUuid, objective1UUID, "a Comment");
        assertThat(objective1.getStatusChanges()).hasSize(1);
        var status = objective1.getStatusChanges().get(0);
        assertThat(status.getComment()).isEqualTo("a Comment");
        assertThat(status.getCreatedBy()).isEqualTo("A user");
        assertThat(status.getStatus()).isEqualTo(CLOSED);
        assertThat(status.getCreated()).isEqualToIgnoringSeconds(LocalDateTime.now());
    }

    @Test
    public void shouldCreateStatusChangeWhenObjectiveIsReOpened() {

        var objective1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1 = new ActionEntity(action1UUID,"Action 1", YearMonth.of(2020,11),null, List.of(PRACTITIONER),null, NOT_STARTED);
        var objective1 = new ObjectiveEntity(objective1UUID, "Objective 1", emptyList(), Map.of(action1UUID, action1),false, CLOSED, 1, LocalDateTime.now(), new ArrayList<>());
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjective(objective1UUID)).thenReturn(objective1);
        when(requestData.getUsername()).thenReturn("A user");
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);
        service.reOpenObjective(sentencePlanUuid, objective1UUID);
        assertThat(objective1.getStatusChanges()).hasSize(1);
        var status = objective1.getStatusChanges().get(0);
        assertThat(status.getComment()).isNull();
        assertThat(status.getCreatedBy()).isEqualTo("A user");
        assertThat(status.getStatus()).isEqualTo(OPEN);
        assertThat(status.getCreated()).isEqualToIgnoringSeconds(LocalDateTime.now());
    }

    @Test
    public void shouldCreateEventWhenObjectiveIsReOpened() {

        var objective1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1 = new ActionEntity(action1UUID,"Action 1", YearMonth.of(2020,11),null, List.of(PRACTITIONER),null, NOT_STARTED);
        var objective1 = new ObjectiveEntity(objective1UUID, "Objective 1", emptyList(), Map.of(action1UUID, action1),false, CLOSED, 1, LocalDateTime.now(), new ArrayList<>());
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjective(objective1UUID)).thenReturn(objective1);
        when(requestData.getUsername()).thenReturn("A user");
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);
        service.reOpenObjective(sentencePlanUuid, objective1UUID);
        verify(timelineService, times(1)).createTimelineEntry(eq(sentencePlanUuid), eq(SENTENCE_PLAN_OBJECTIVE_REOPENED), any(ObjectiveEntity.class));
    }

    @Test
    public void shouldNotCreateEventWhenObjectiveIsAlreadyOpen() {

        var objective1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var action1 = new ActionEntity(action1UUID,"Action 1", YearMonth.of(2020,11),null, List.of(PRACTITIONER),null, NOT_STARTED);
        var objective1 = new ObjectiveEntity(objective1UUID, "Objective 1", emptyList(), Map.of(action1UUID, action1),false, OPEN, 1, LocalDateTime.now(), new ArrayList<>());
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjective(objective1UUID)).thenReturn(objective1);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);
        service.reOpenObjective(sentencePlanUuid, objective1UUID);
        verify(timelineService, never()).createTimelineEntry(eq(sentencePlanUuid), eq(SENTENCE_PLAN_OBJECTIVE_REOPENED), any(ObjectiveEntity.class));
    }

    @Test
    public void shouldAddActionToSentencePlanObjective() {

        var action = ArgumentCaptor.forClass(ActionEntity.class);
        var newSentencePlan = mock(SentencePlanEntity.class);
        var objective = mock(ObjectiveEntity.class);
        var objectiveUUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

        when(newSentencePlan.getObjective(objectiveUUID)).thenReturn(objective);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);

        var motivationUuid = motivations.get(0).getUuid();

        var request = new AddSentencePlanActionRequest(null, "Action 1", YearMonth.of(2019, 10), motivationUuid,  List.of(SERVICE_USER), null, NOT_STARTED);
        service.addAction(sentencePlanUuid,objectiveUUID, request);

        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
        verify(objective,times(1)).addAction(action.capture());
        verify(timelineService, times(1)).createTimelineEntry(eq(sentencePlanUuid), eq(SENTENCE_PLAN_ACTION_CREATED), any(ObjectiveEntity.class));

        assertThat(action.getValue().getProgress()).isEmpty();
        assertThat(action.getValue().getDescription()).isEqualTo("Action 1");
        assertThat(action.getValue().getInterventionUUID()).isEqualTo(null);
        assertThat(action.getValue().getOwnerOther()).isEqualTo(null);
        assertThat(action.getValue().getOwner()).containsExactlyInAnyOrder(SERVICE_USER);
        assertThat(action.getValue().getStatus()).isEqualTo(NOT_STARTED);
        assertThat(action.getValue().getMotivationUUID()).isEqualTo(motivationUuid);

    }

    @Test
    public void shouldGetObjectivesForSentencePlan() {

        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getSentencePlanWithOneObjectiveOneAction());
        var objectives = service.getSentencePlanObjectives(sentencePlanUuid);

        assertThat(objectives.size()).isEqualTo(1);
        var objective = objectives.stream().findFirst().get();
        assertThat(objective.getDescription()).isEqualTo("Objective 1");
        assertThat(objective.getNeeds().get(0)).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
    }


    @Test
    public void updateObjectivePriorityShouldNotChangeOrderWhenEmpty() {
        var objective1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var objective2UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
        var objective1 = new ObjectiveEntity(objective1UUID, "Objective 1", emptyList(), emptyMap(),false, OPEN, 1, LocalDateTime.now(), emptyList());
        var objective2 = new ObjectiveEntity(objective2UUID, "Objective 2", emptyList(), emptyMap(),false, OPEN, 2, LocalDateTime.now(), emptyList());
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjectives()).thenReturn(Map.of(objective1UUID, objective1, objective2UUID, objective2));
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);

        assertThat(objective1.getPriority()).isEqualTo(1);
        assertThat(objective2.getPriority()).isEqualTo(2);

        service.updateObjectivePriorities(sentencePlanUuid, emptyList());

        assertThat(objective1.getPriority()).isEqualTo(1);
        assertThat(objective2.getPriority()).isEqualTo(2);
    }


    @Test
    public void updateObjectivePriorityShouldChangePriority() {
        var objective1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var objective2UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
        var objective1 = new ObjectiveEntity(objective1UUID, "Objective 1", emptyList(), emptyMap(),false, OPEN, 1, LocalDateTime.now(), emptyList());
        var objective2 = new ObjectiveEntity(objective2UUID, "Objective 2", emptyList(), emptyMap(),false, OPEN, 2, LocalDateTime.now(), emptyList());
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjectives()).thenReturn(Map.of(objective1UUID, objective1, objective2UUID, objective2));
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);

        assertThat(objective1.getPriority()).isEqualTo(1);
        assertThat(objective2.getPriority()).isEqualTo(2);

        var updatedPriorities = List.of(new UpdateObjectivePriorityRequest(objective1UUID, 2), new UpdateObjectivePriorityRequest(objective2UUID, 1)) ;
        service.updateObjectivePriorities(sentencePlanUuid, updatedPriorities);

        assertThat(objective1.getPriority()).isEqualTo(2);
        assertThat(objective2.getPriority()).isEqualTo(1);
    }

    @Test
    public void updateObjectivePriorityShouldIgnoreMissingObjectiveUUIDs() {
        var objective1UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var objective2UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
        var objective1 = new ObjectiveEntity(objective1UUID, "Objective 1", emptyList(), emptyMap(),false, OPEN, 1, LocalDateTime.now(), emptyList());
        var objective2 = new ObjectiveEntity(objective2UUID, "Objective 2", emptyList(), emptyMap(),false, OPEN, 2, LocalDateTime.now(), emptyList());
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjectives()).thenReturn(Map.of(objective1UUID, objective1, objective2UUID, objective2));
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);

        assertThat(objective1.getPriority()).isEqualTo(1);
        assertThat(objective2.getPriority()).isEqualTo(2);

        var updatedPriorities = List.of(new UpdateObjectivePriorityRequest(objective1UUID, 3)) ;
        service.updateObjectivePriorities(sentencePlanUuid, updatedPriorities);

        assertThat(objective1.getPriority()).isEqualTo(3);
        assertThat(objective2.getPriority()).isEqualTo(2);  }

    @Test
    public void shouldNotUpdateActionWhenNotDraft(){
        var objectiveUUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.isDraft()).thenReturn(false);

        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);
        var request = new AddSentencePlanActionRequest(UUID.randomUUID(), "Any Desc", YearMonth.now(), UUID.randomUUID(), emptyList(), "Other Owner", ActionStatus.PARTIALLY_COMPLETED);
        var exception = catchThrowable(() -> service.updateAction(sentencePlanUuid, objectiveUUID,UUID.randomUUID(), request));
        assertThat(exception).isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot update Action, Sentence Plan is not a draft");

    }

    @Test
    public void updateActionPriorityShouldNotChangeOrderWhenEmpty() {
        var objective = getObjectiveWithTwoActions(emptyList(), "Objective 1", "Action 1", "Action 2");
        var objectiveUUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjective(objectiveUUID)).thenReturn(objective);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);

        assertThat(objective.getAction(UUID.fromString("11111111-1111-1111-1111-111111111111")).getPriority()).isEqualTo(1);
        assertThat(objective.getAction(UUID.fromString("22222222-2222-2222-2222-222222222222")).getPriority()).isEqualTo(2);

        service.updateActionPriorities(sentencePlanUuid, objectiveUUID,emptyList());

        assertThat(objective.getAction(UUID.fromString("11111111-1111-1111-1111-111111111111")).getPriority()).isEqualTo(1);
        assertThat(objective.getAction(UUID.fromString("22222222-2222-2222-2222-222222222222")).getPriority()).isEqualTo(2);
    }

    @Test
    public void updateActionPriorityShouldChangePriority() {
        var objective = getObjectiveWithTwoActions(emptyList(), "Objective 1", "Action 1", "Action 2");
        var objectiveUUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjective(objectiveUUID)).thenReturn(objective);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);

        assertThat(objective.getAction(UUID.fromString("11111111-1111-1111-1111-111111111111")).getPriority()).isEqualTo(1);
        assertThat(objective.getAction(UUID.fromString("22222222-2222-2222-2222-222222222222")).getPriority()).isEqualTo(2);

        var updatedPriorities = List.of(new UpdateActionPriorityRequest(UUID.fromString("11111111-1111-1111-1111-111111111111"), 2), new UpdateActionPriorityRequest(UUID.fromString("22222222-2222-2222-2222-222222222222"), 1)) ;

        service.updateActionPriorities(sentencePlanUuid, objectiveUUID,updatedPriorities);

        assertThat(objective.getAction(UUID.fromString("11111111-1111-1111-1111-111111111111")).getPriority()).isEqualTo(2);
        assertThat(objective.getAction(UUID.fromString("22222222-2222-2222-2222-222222222222")).getPriority()).isEqualTo(1);
    }

    @Test
    public void updateActionPriorityShouldIgnoreMissingActionUUIDs() {
        var objective = getObjectiveWithTwoActions(emptyList(), "Objective 1", "Action 1", "Action 2");
        var objectiveUUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var newSentencePlan = mock(SentencePlanEntity.class);
        when(newSentencePlan.getObjective(objectiveUUID)).thenReturn(objective);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(newSentencePlan);

        assertThat(objective.getAction(UUID.fromString("11111111-1111-1111-1111-111111111111")).getPriority()).isEqualTo(1);
        assertThat(objective.getAction(UUID.fromString("22222222-2222-2222-2222-222222222222")).getPriority()).isEqualTo(2);

        var updatedPriorities = List.of(new UpdateActionPriorityRequest(UUID.fromString("22222222-2222-2222-2222-222222222222"), 3)) ;
        service.updateActionPriorities(sentencePlanUuid, objectiveUUID,updatedPriorities);

        assertThat(objective.getAction(UUID.fromString("11111111-1111-1111-1111-111111111111")).getPriority()).isEqualTo(1);
        assertThat(objective.getAction(UUID.fromString("22222222-2222-2222-2222-222222222222")).getPriority()).isEqualTo(3);
    }

    @Test
    public void getSentencePlansForOffenderShouldReturnOASysPlans() {

        var offender = getOffenderEntity();
        var legacyPlan =  OasysSentencePlanDto.builder()
                .completedDate(LocalDate.of(2019,1,1))
                .createdDate(LocalDate.of(2018,1,1))
                .oasysSetId(123456L).build();

        when(oasysAssessmentAPIClient.getSentencePlansForOffender(12345L)).thenReturn(List.of(legacyPlan));
        when(offenderService.getOasysOffender(12345L)).thenReturn(offender);
        when(sentencePlanRepository.findByOffenderUuid(offender.getUuid())).thenReturn(Collections.emptyList());

        var result = service.getSentencePlansForOffender(12345L).get(0);

        assertThat(result.getPlanId()).isEqualTo("123456");
        assertThat(result.getCreatedDate()).isEqualTo(LocalDate.of(2018,1,1));
        assertThat(result.getCompletedDate()).isEqualTo (LocalDate.of(2019,1,1));
        verify(oasysAssessmentAPIClient, times(1)).getSentencePlansForOffender(12345L);

    }

    @Test
    public void getLegacySentencePlanShouldReturnPlanForId() {

        var legacyPlan1 =  OasysSentencePlanDto.builder()
                .completedDate(LocalDate.of(2019,1,1))
                .createdDate(LocalDate.of(2018,1,1))
                .oasysSetId(1L).build();

        var legacyPlan2 =  OasysSentencePlanDto.builder()
                .completedDate(LocalDate.of(2018,1,1))
                .createdDate(LocalDate.of(2017,1,1))
                .oasysSetId(2L).build();

        when(oasysAssessmentAPIClient.getSentencePlansForOffender(12345L)).thenReturn(List.of(legacyPlan1, legacyPlan2));

        var result = service.getLegacySentencePlan(12345L, "1");

        assertThat(result.getOasysSetId()).isEqualTo(1L);
        assertThat(result.getCompletedDate()).isEqualTo(LocalDate.of(2019,1,1));
        assertThat(result.getCreatedDate()).isEqualTo(LocalDate.of(2018,1,1));
    }


    @Test
    public void getSentencePlansForOffenderShouldReturnNewPlans() {

        var offender = getOffenderEntity();
        when(oasysAssessmentAPIClient.getSentencePlansForOffender(12345L)).thenReturn(Collections.emptyList());
        when(offenderService.getOasysOffender(12345L)).thenReturn(offender);
        when(sentencePlanRepository.findByOffenderUuid(offender.getUuid())).thenReturn(List.of(getNewSentencePlan(sentencePlanUuid)));

        var result = service.getSentencePlansForOffender(12345L).get(0);

        assertThat(result.getPlanId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(result.getCompletedDate()).isNull();
        verify(sentencePlanRepository, times(1)).findByOffenderUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    }

    @Test
    public void getSentencePlansForOffenderShouldReturnDraftStatusFalseIfPlanHasStarted() {

        var offender = getOffenderEntity();
        var plan = getNewSentencePlan(sentencePlanUuid);
        plan.setStartedDate(LocalDateTime.of(2019,11,11,9,0));
        when(oasysAssessmentAPIClient.getSentencePlansForOffender(12345L)).thenReturn(Collections.emptyList());
        when(offenderService.getOasysOffender(12345L)).thenReturn(offender);
        when(sentencePlanRepository.findByOffenderUuid(offender.getUuid())).thenReturn(List.of(plan));

        var result = service.getSentencePlansForOffender(12345L).get(0);

        assertThat(result.getPlanId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(result.isDraft()).isFalse();
        verify(sentencePlanRepository, times(1)).findByOffenderUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    }

    @Test
    public void getSentencePlansForOffenderShouldReturnDraftStatusTrueIfPlanHasNotStarted() {

        var offender = getOffenderEntity();
        var plan = getNewSentencePlan(sentencePlanUuid);
        plan.setStartedDate(null);
        when(oasysAssessmentAPIClient.getSentencePlansForOffender(12345L)).thenReturn(Collections.emptyList());
        when(offenderService.getOasysOffender(12345L)).thenReturn(offender);
        when(sentencePlanRepository.findByOffenderUuid(offender.getUuid())).thenReturn(List.of(plan));

        var result = service.getSentencePlansForOffender(12345L).get(0);

        assertThat(result.getPlanId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(result.isDraft()).isTrue();
        verify(sentencePlanRepository, times(1)).findByOffenderUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));

    }

    @Test
    public void getSentencePlansForOffenderShouldOrderPlansByCreatedDate() {

        var offender = getOffenderEntity();
        var legacyPlan =  OasysSentencePlanDto.builder()
                .completedDate(LocalDate.of(2019,1,1))
                .createdDate(LocalDate.of(2018,1,1))
                .oasysSetId(123456L).build();
        when(oasysAssessmentAPIClient.getSentencePlansForOffender(12345L)).thenReturn(List.of(legacyPlan));
        when(offenderService.getOasysOffender(12345L)).thenReturn(offender);

        var sentencePlan1 = getNewSentencePlan(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        var sentencePlan2 = getNewSentencePlan(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        var sentencePlan3 = getNewSentencePlan(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        sentencePlan1.setCreatedOn(LocalDateTime.of(2019,1,2,1,0));
        sentencePlan2.setCreatedOn(LocalDateTime.of(2019,1,1,1,0));
        sentencePlan3.setCreatedOn(LocalDateTime.of(2019,1,3,1,0));

        when(sentencePlanRepository.findByOffenderUuid(offender.getUuid())).thenReturn(List.of(sentencePlan1,sentencePlan2,sentencePlan3));

        var result = service.getSentencePlansForOffender(12345L);
        assertThat(result).hasSize(4);

        assertThat(result.get(3).getPlanId()).isEqualTo("123456");
        assertThat(result.get(2).getPlanId()).isEqualTo("22222222-2222-2222-2222-222222222222");
        assertThat(result.get(1).getPlanId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(result.get(0).getPlanId()).isEqualTo("33333333-3333-3333-3333-333333333333");

    }

    @Test
    public void addCommentsShouldAddAllCommentsToPlan() {
        var sentencePlan = mock(SentencePlanEntity.class);
        var comments = ArgumentCaptor.forClass(CommentEntity.class);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        var comment1 = new AddCommentRequest("Comment 1", LIAISON_ARRANGEMENTS);
        var comment2 = new AddCommentRequest("Comment 2", YOUR_SUMMARY);
        service.addSentencePlanComments(sentencePlanUuid, List.of(comment1));
        service.addSentencePlanComments(sentencePlanUuid, List.of(comment2));
        verify(sentencePlan,times(2)).addComment(comments.capture());
        verify(timelineService,times(2)).createTimelineEntry(eq(sentencePlanUuid), eq(SENTENCE_PLAN_COMMENTS_CREATED), comments.capture());

        var result = comments.getAllValues();

        assertThat(result.get(0).getCommentType()).isEqualTo(LIAISON_ARRANGEMENTS);
        assertThat(result.get(0).getComment()).isEqualTo("Comment 1");

        assertThat(result.get(1).getCommentType()).isEqualTo(YOUR_SUMMARY);
        assertThat(result.get(1).getComment()).isEqualTo("Comment 2");
    }

    @Test
    public void addNoCommentsShouldNotSaveToRepository() {
        var sentencePlan = mock(SentencePlanEntity.class);
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(sentencePlan);

        service.addSentencePlanComments(sentencePlanUuid, List.of());

        verify(sentencePlan,never()).addComment(any());
    }

    private SentencePlanEntity getNewSentencePlan(UUID uuid) {
        var needs = List.of(NeedEntity.builder().uuid(UUID.fromString("11111111-1111-1111-1111-111111111111")).description("description").build());
        var plan = new  SentencePlanEntity();
        plan.setUuid(uuid);
        plan.setNeeds(needs);
        return plan;
    }

    private SentencePlanEntity getSentencePlanWithOneObjectiveOneAction() {

        var needs = List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        var sentencePlanProperty = new SentencePlanPropertiesEntity();
        var objective = new ObjectiveEntity("Objective 1", needs, false);
        var action = new ActionEntity(UUID.fromString("11111111-1111-1111-1111-111111111111"),null,"Action 1", YearMonth.of(2019,8),
                UUID.fromString("11111111-1111-1111-1111-111111111111"), List.of(SERVICE_USER), null, NOT_STARTED, 1, emptyList(),
                LocalDateTime.of(2019,6,6, 2,0),null);
        var offender = new OffenderEntity(1L, "two", "3");
        objective.addAction(action);
        sentencePlanProperty.setObjectives(Map.of(objective.getId(), objective));

        return new SentencePlanEntity(1L,sentencePlanUuid,
                LocalDateTime.of(2019,6,1, 11,0),
                LocalDateTime.of(2019,7,1, 11,0),
                LocalDateTime.of(2019,7,1, 11,0),
                "any user",
                LocalDateTime.of(2019,7,1, 11,0),
                null,sentencePlanProperty, null, offender, null);


    }

    private ObjectiveEntity getObjectiveWithTwoActions(List<UUID> needs, String objectiveDescription, String action1Description, String action2Description) {
        var objective = new ObjectiveEntity(objectiveDescription, needs, false);
        var action1 = new ActionEntity(UUID.fromString("11111111-1111-1111-1111-111111111111"),null,action1Description, YearMonth.of(2019,8),
                UUID.fromString("11111111-1111-1111-1111-111111111111"), List.of(SERVICE_USER), null, NOT_STARTED, 1, emptyList(),
                LocalDateTime.of(2019,6,6, 2, 0),null);
        var action2 = new ActionEntity(UUID.fromString("22222222-2222-2222-2222-222222222222"),null,action2Description, YearMonth.of(2019,8),
                UUID.fromString("11111111-1111-1111-1111-111111111111"), List.of(PRACTITIONER), null, NOT_STARTED, 2, emptyList(),
                LocalDateTime.of(2019,6,6, 2,10),null );

        objective.addAction(action1);
        objective.addAction(action2);
        return objective;
    }

    private OffenderEntity getOffenderEntity() {
        return new OffenderEntity(1L, UUID.fromString("11111111-1111-1111-1111-111111111111"), 12345L, null, null, "123", LocalDateTime.now(), Collections.emptyList());
    }
}