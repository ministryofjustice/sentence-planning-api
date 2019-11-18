package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus;
import uk.gov.digital.justice.hmpps.sentenceplan.application.ValidationException;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.OTHER;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.PRACTITIONER;

@RunWith(MockitoJUnitRunner.class)
public class ActionEntityTest {

    private static final List<ActionOwner> owner = List.of(ActionOwner.SERVICE_USER);
    private static final String description = "Description";
    private static final UUID motivation = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID intervention = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final ActionStatus status = ActionStatus.IN_PROGRESS;
    private static final YearMonth targetDate = YearMonth.of(2019,6);


    private static final List<ActionOwner> ownerUpdate = List.of(PRACTITIONER);
    private static final String descriptionUpdate = "DescriptionU";
    private static final String strengthUpdate = "StrengthU";
    private static final UUID interventionUpdate = UUID.fromString("11111111-1111-1111-1111-111111111112");
    private static final ActionStatus statusUpdate = ActionStatus.PARTIALLY_COMPLETED;

    @Test
    public void shouldCreateAction() {

        var action = createValidAction();

        assertThat(action.getOwner()).isEqualTo(owner);
        assertThat(action.getOwnerOther()).isNull();
        assertThat(action.getDescription()).isEqualTo(description);
        assertThat(action.getStatus()).isEqualTo(status);
        assertThat(action.getInterventionUUID()).isEqualTo(null);
    }


    @Test(expected = ValidationException.class)
    public void shouldNotCreateActionWithOwnerNull() {
        new ActionEntity(null, description, targetDate,motivation,null,null, status);
    }

    @Test
    public void shouldCreateActionWithOtherOwner() {
        var action = new ActionEntity(null, description, targetDate,motivation, List.of(OTHER),"Other", status);

        assertThat(action.getOwner()).hasSize(1);
        assertThat(action.getOwner()).contains(OTHER);
        assertThat(action.getOwnerOther()).isEqualTo("Other");
    }

    @Test(expected = ValidationException.class)
    public void shouldNotCreateActionWithOtherOwnerNull() {
       new ActionEntity(null, description, targetDate,motivation, List.of(OTHER),null, status);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotCreateActionWithNoDescriptionAndIntervention() {
        new ActionEntity(null, null, targetDate,motivation, List.of(OTHER),"Other", status);

    }

    @Test
    public void shouldCreateActionWithDescriptionNoIntervention() {
        var action = new ActionEntity(null, description, targetDate,motivation, List.of(OTHER),"Other", status);
        assertThat(action.getDescription()).isEqualTo(description);
        assertThat(action.getInterventionUUID()).isEqualTo(null);
    }

    @Test
    public void shouldCreateActionWithDescriptionAndIntervention() {
        var action = new ActionEntity(intervention, description, targetDate,motivation, List.of(OTHER),"Other", status);
        assertThat(action.getDescription()).isEqualTo(description);
        assertThat(action.getInterventionUUID()).isEqualTo(intervention);
    }

    @Test
    public void shouldCreateActionWithNoDescriptionButAnIntervention() {
        var action = new ActionEntity(intervention, null, targetDate,motivation, List.of(OTHER),"Other", status);
        assertThat(action.getDescription()).isEqualTo(null);
        assertThat(action.getInterventionUUID()).isEqualTo(intervention);
    }


    @Test(expected = ValidationException.class)
    public void shouldNotUpdateActionWithOwnerNull() {
        var action = createValidAction();
        action.updateAction(intervention, description, targetDate,motivation, null,null, status);

    }

    @Test
    public void shouldUpdateActionWithOtherOwner() {
        var action = createValidAction();
        action.updateAction(intervention, description, targetDate,motivation, List.of(OTHER),"Other", status);

        assertThat(action.getOwner()).hasSize(1);
        assertThat(action.getOwner()).contains(OTHER);
        assertThat(action.getOwnerOther()).isEqualTo("Other");
    }

    @Test
    public void shouldUpdateActionWithOtherOwnerMultiple() {
        var action = createValidAction();
        action.updateAction(intervention, description, targetDate,motivation, List.of(OTHER, PRACTITIONER),"Other", status);

        assertThat(action.getOwner()).hasSize(2);
        assertThat(action.getOwner()).contains(PRACTITIONER);
        assertThat(action.getOwner()).contains(OTHER);
        assertThat(action.getOwnerOther()).isEqualTo("Other");
    }

    @Test(expected = ValidationException.class)
    public void shouldNotUpdateActionWithOtherOwnerNull() {
        var action = createValidAction();
        action.updateAction(intervention, description, targetDate,motivation, List.of(OTHER),null, status);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotUpdateActionWithNoDescriptionAndIntervention() {
        var action = createValidAction();
        action.updateAction(null, null, targetDate,motivation, List.of(PRACTITIONER),null, status);
    }

    @Test
    public void shouldUpdateActionWithDescriptionNoIntervention() {
        var action = createValidAction();
        action.updateAction(null, descriptionUpdate, targetDate,motivation, List.of(PRACTITIONER),null, status);

        assertThat(action.getDescription()).isEqualTo(descriptionUpdate);
        assertThat(action.getInterventionUUID()).isEqualTo(null);
    }

    @Test
    public void shouldUpdateActionWithDescriptionAndIntervention() {
        var action = createValidAction();
        action.updateAction(interventionUpdate, descriptionUpdate, targetDate,motivation, List.of(PRACTITIONER),null, status);

        assertThat(action.getDescription()).isEqualTo(descriptionUpdate);
        assertThat(action.getInterventionUUID()).isEqualTo(interventionUpdate);
    }

    @Test
    public void shouldUpdateActionWithNoDescriptionButAnIntervention() {
        var action = createValidAction();
        action.updateAction(interventionUpdate, null, targetDate,motivation, List.of(PRACTITIONER),null, status);

        assertThat(action.getDescription()).isEqualTo(null);
        assertThat(action.getInterventionUUID()).isEqualTo(interventionUpdate);
    }

    @Test
    public void shouldUpdateActionPriority() {

        var action = createValidAction();

        assertThat(action.getPriority()).isEqualTo(0);

        action.setPriority(5);

        assertThat(action.getPriority()).isEqualTo(5);
    }

    @Test
    public void shouldAddProgress() {

        var action = createValidAction();

        assertThat(action.getStatus()).isEqualTo(status);

        var newProgress = new ProgressEntity(ActionStatus.COMPLETED, targetDate,motivation,"Comment", LocalDateTime.of(2019,6,6, 9,0),"a person");
        action.addProgress(newProgress);

        assertThat(action.getStatus()).isEqualTo(ActionStatus.COMPLETED);
        assertThat(action.getUpdated()).isEqualTo(newProgress.getCreated());
        assertThat(action.getProgress()).hasSize(1);
    }

    private static ActionEntity createValidAction() {
        return new ActionEntity(null, description, targetDate,motivation,owner,null, status);
    }

}