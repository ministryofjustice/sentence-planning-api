package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus;
import uk.gov.digital.justice.hmpps.sentenceplan.application.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ActionEntityTest {

    private static final List<ActionOwner> owner = List.of(ActionOwner.SERVICE_USER);
    private static final String description = "Description";
    private static final String strength = "Strength";
    private static final String intervention = "Intervention";
    private static final ActionStatus status = ActionStatus.IN_PROGRESS;

    private static final List<ActionOwner> ownerUpdate = List.of(ActionOwner.PRACTITIONER);
    private static final String descriptionUpdate = "DescriptionU";
    private static final String strengthUpdate = "StrengthU";
    private static final String interventionUpdate = "InterventionU";
    private static final ActionStatus statusUpdate = ActionStatus.PARTIALLY_COMPLETED;

    @Test
    public void shouldCreateActionWithNeeds() {

        var action = createValidAction();

        assertThat(action.getOwner()).isEqualTo(owner);
        assertThat(action.getOwnerOther()).isNull();
        assertThat(action.getDescription()).isEqualTo(description);
        assertThat(action.getStrength()).isEqualTo(strength);
        assertThat(action.getStatus()).isEqualTo(status);
        assertThat(action.getIntervention()).isEqualTo(null);
        assertThat(action.getNeeds()).hasSize(3);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotCreateActionWithoutNeedsNull() {
        new ActionEntity(owner, null, description, strength, status, null , null);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotCreateActionWithoutNeedsEmpty() {
        new ActionEntity(owner, null, description, strength, status, new ArrayList<>(), null);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotCreateActionWithOwnerNull() {
        new ActionEntity(null, null, description, strength, status, createNeedList() , null);
    }

    @Test
    public void shouldCreateActionWithOtherOwner() {
        var action = new ActionEntity(List.of(ActionOwner.OTHER), "Nurse", description, strength, status, createNeedList() , null);

        assertThat(action.getOwner()).hasSize(1);
        assertThat(action.getOwner()).contains(ActionOwner.OTHER);
        assertThat(action.getOwnerOther()).isEqualTo("Nurse");
    }

    @Test(expected = ValidationException.class)
    public void shouldNotCreateActionWithOtherOwnerNull() {
       new ActionEntity(List.of(ActionOwner.OTHER), null, description, strength, status, createNeedList() , null);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotCreateActionWithNoDescriptionAndIntervention() {
        new ActionEntity(owner, null, "", strength, status, createNeedList() , "");
    }

    @Test
    public void shouldCreateActionWithDescriptionNoIntervention() {
        var action = new ActionEntity(owner, null, description, strength, status, createNeedList() , null);

        assertThat(action.getDescription()).isEqualTo(description);
        assertThat(action.getIntervention()).isEqualTo(null);
    }

    @Test
    public void shouldCreateActionWithDescriptionAndIntervention() {
        var action = new ActionEntity(owner, null, description, strength, status, createNeedList() , intervention);

        assertThat(action.getDescription()).isEqualTo(intervention);
        assertThat(action.getIntervention()).isEqualTo(intervention);
    }

    @Test
    public void shouldCreateActionWithNoDescriptionButAnIntervention() {
        var action = new ActionEntity(owner, null, null, strength, status, createNeedList() , intervention);

        assertThat(action.getDescription()).isEqualTo(intervention);
        assertThat(action.getIntervention()).isEqualTo(intervention);
    }

    @Test
    public void shouldUpdateActionWithNeeds() {

        var action = createValidAction();

        assertThat(action.getOwner()).isEqualTo(owner);
        assertThat(action.getOwnerOther()).isNull();
        assertThat(action.getDescription()).isEqualTo(description);
        assertThat(action.getStrength()).isEqualTo(strength);
        assertThat(action.getStatus()).isEqualTo(status);
        assertThat(action.getIntervention()).isEqualTo(null);
        assertThat(action.getNeeds()).hasSize(3);

        action.updateAction(ownerUpdate, null, descriptionUpdate, strengthUpdate, statusUpdate, createNeedListUpdate(), null);

        assertThat(action.getOwner()).isEqualTo(ownerUpdate);
        assertThat(action.getOwnerOther()).isNull();
        assertThat(action.getDescription()).isEqualTo(descriptionUpdate);
        assertThat(action.getStrength()).isEqualTo(strengthUpdate);
        assertThat(action.getStatus()).isEqualTo(statusUpdate);
        assertThat(action.getIntervention()).isEqualTo(null);
        assertThat(action.getNeeds()).hasSize(2);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotUpdateActionWithoutNeedsNull() {
        var action = createValidAction();
        action.updateAction(ownerUpdate, null, descriptionUpdate, strengthUpdate, statusUpdate, null, null);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotUpdateActionWithoutNeedsEmpty() {
        var action = createValidAction();
        action.updateAction(ownerUpdate, null, descriptionUpdate, strengthUpdate, statusUpdate, new ArrayList<>(), null);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotUpdateActionWithOwnerNull() {
        var action = createValidAction();
        action.updateAction(null, null, descriptionUpdate, strengthUpdate, statusUpdate, createNeedListUpdate(), null);
    }

    @Test
    public void shouldUpdateActionWithOtherOwner() {
        var action = createValidAction();
        action.updateAction(List.of(ActionOwner.OTHER), "Nurse", descriptionUpdate, strengthUpdate, statusUpdate, createNeedListUpdate(), null);

        assertThat(action.getOwner()).hasSize(1);
        assertThat(action.getOwner()).contains(ActionOwner.OTHER);
        assertThat(action.getOwnerOther()).isEqualTo("Nurse");
    }

    @Test
    public void shouldUpdateActionWithOtherOwnerMultiple() {
        var action = createValidAction();
        action.updateAction(List.of(ActionOwner.PRACTITIONER, ActionOwner.OTHER), "Nurse", descriptionUpdate, strengthUpdate, statusUpdate, createNeedListUpdate(), null);

        assertThat(action.getOwner()).hasSize(2);
        assertThat(action.getOwner()).contains(ActionOwner.PRACTITIONER);
        assertThat(action.getOwner()).contains(ActionOwner.OTHER);
        assertThat(action.getOwnerOther()).isEqualTo("Nurse");
    }

    @Test(expected = ValidationException.class)
    public void shouldNotUpdateActionWithOtherOwnerNull() {
        var action = createValidAction();
        action.updateAction(List.of(ActionOwner.OTHER), null, descriptionUpdate, strengthUpdate, statusUpdate, null, null);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotUpdateActionWithNoDescriptionAndIntervention() {
        var action = createValidAction();
        action.updateAction(ownerUpdate, null, "", strengthUpdate, statusUpdate, null, "");
    }

    @Test
    public void shouldUpdateActionWithDescriptionNoIntervention() {
        var action = createValidAction();
        action.updateAction(ownerUpdate, null, descriptionUpdate, strengthUpdate, statusUpdate, createNeedListUpdate(), null);

        assertThat(action.getDescription()).isEqualTo(descriptionUpdate);
        assertThat(action.getIntervention()).isEqualTo(null);
    }

    @Test
    public void shouldUpdateActionWithDescriptionAndIntervention() {
        var action = createValidAction();
        action.updateAction(ownerUpdate, null, descriptionUpdate, strengthUpdate, statusUpdate, createNeedListUpdate(), interventionUpdate);

        assertThat(action.getDescription()).isEqualTo(interventionUpdate);
        assertThat(action.getIntervention()).isEqualTo(interventionUpdate);
    }

    @Test
    public void shouldUpdateActionWithNoDescriptionButAnIntervention() {
        var action = createValidAction();
        action.updateAction(ownerUpdate, null, null, strengthUpdate, statusUpdate, createNeedListUpdate(), interventionUpdate);

        assertThat(action.getDescription()).isEqualTo(interventionUpdate);
        assertThat(action.getIntervention()).isEqualTo(interventionUpdate);
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

        var newProgress = new ProgressEntity(ActionStatus.COMPLETED, "", LocalDateTime.now(), "");
        action.addProgress(newProgress);

        assertThat(action.getStatus()).isEqualTo(ActionStatus.COMPLETED);
        assertThat(action.getLatestUpdated()).isEqualTo(newProgress.getCreated());
        assertThat(action.getProgress()).hasSize(1);
    }

    private static ActionEntity createValidAction() {
        return new ActionEntity(owner, null, description, strength, status, createNeedList() , null);
    }

    private static List<UUID> createNeedList(){
        return List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }

    private static List<UUID> createNeedListUpdate(){
        return List.of(UUID.randomUUID(), UUID.randomUUID());
    }
}