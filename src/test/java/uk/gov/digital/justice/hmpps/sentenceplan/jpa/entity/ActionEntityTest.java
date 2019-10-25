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
    public void shouldCreateStepWithNeeds() {

        var step = createValidStep();

        assertThat(step.getOwner()).isEqualTo(owner);
        assertThat(step.getOwnerOther()).isNull();
        assertThat(step.getDescription()).isEqualTo(description);
        assertThat(step.getStrength()).isEqualTo(strength);
        assertThat(step.getStatus()).isEqualTo(status);
        assertThat(step.getIntervention()).isEqualTo(null);
        assertThat(step.getNeeds()).hasSize(3);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotCreateStepWithoutNeedsNull() {
        new ActionEntity(owner, null, description, strength, status, null , null);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotCreateStepWithoutNeedsEmpty() {
        new ActionEntity(owner, null, description, strength, status, new ArrayList<>(), null);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotCreateStepWithOwnerNull() {
        new ActionEntity(null, null, description, strength, status, createNeedList() , null);
    }

    @Test
    public void shouldCreateStepWithOtherOwner() {
        var step = new ActionEntity(List.of(ActionOwner.OTHER), "Nurse", description, strength, status, createNeedList() , null);

        assertThat(step.getOwner()).hasSize(1);
        assertThat(step.getOwner()).contains(ActionOwner.OTHER);
        assertThat(step.getOwnerOther()).isEqualTo("Nurse");
    }

    @Test(expected = ValidationException.class)
    public void shouldNotCreateStepWithOtherOwnerNull() {
       new ActionEntity(List.of(ActionOwner.OTHER), null, description, strength, status, createNeedList() , null);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotCreateStepWithNoDescriptionAndIntervention() {
        new ActionEntity(owner, null, "", strength, status, createNeedList() , "");
    }

    @Test
    public void shouldCreateStepWithDescriptionNoIntervention() {
        var step = new ActionEntity(owner, null, description, strength, status, createNeedList() , null);

        assertThat(step.getDescription()).isEqualTo(description);
        assertThat(step.getIntervention()).isEqualTo(null);
    }

    @Test
    public void shouldCreateStepWithDescriptionAndIntervention() {
        var step = new ActionEntity(owner, null, description, strength, status, createNeedList() , intervention);

        assertThat(step.getDescription()).isEqualTo(intervention);
        assertThat(step.getIntervention()).isEqualTo(intervention);
    }

    @Test
    public void shouldCreateStepWithNoDescriptionButAnIntervention() {
        var step = new ActionEntity(owner, null, null, strength, status, createNeedList() , intervention);

        assertThat(step.getDescription()).isEqualTo(intervention);
        assertThat(step.getIntervention()).isEqualTo(intervention);
    }

    @Test
    public void shouldUpdateStepWithNeeds() {

        var step = createValidStep();

        assertThat(step.getOwner()).isEqualTo(owner);
        assertThat(step.getOwnerOther()).isNull();
        assertThat(step.getDescription()).isEqualTo(description);
        assertThat(step.getStrength()).isEqualTo(strength);
        assertThat(step.getStatus()).isEqualTo(status);
        assertThat(step.getIntervention()).isEqualTo(null);
        assertThat(step.getNeeds()).hasSize(3);

        step.updateStep(ownerUpdate, null, descriptionUpdate, strengthUpdate, statusUpdate, createNeedListUpdate(), null);

        assertThat(step.getOwner()).isEqualTo(ownerUpdate);
        assertThat(step.getOwnerOther()).isNull();
        assertThat(step.getDescription()).isEqualTo(descriptionUpdate);
        assertThat(step.getStrength()).isEqualTo(strengthUpdate);
        assertThat(step.getStatus()).isEqualTo(statusUpdate);
        assertThat(step.getIntervention()).isEqualTo(null);
        assertThat(step.getNeeds()).hasSize(2);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotUpdateStepWithoutNeedsNull() {
        var step = createValidStep();
        step.updateStep(ownerUpdate, null, descriptionUpdate, strengthUpdate, statusUpdate, null, null);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotUpdateStepWithoutNeedsEmpty() {
        var step = createValidStep();
        step.updateStep(ownerUpdate, null, descriptionUpdate, strengthUpdate, statusUpdate, new ArrayList<>(), null);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotUpdateStepWithOwnerNull() {
        var step = createValidStep();
        step.updateStep(null, null, descriptionUpdate, strengthUpdate, statusUpdate, createNeedListUpdate(), null);
    }

    @Test
    public void shouldUpdateStepWithOtherOwner() {
        var step = createValidStep();
        step.updateStep(List.of(ActionOwner.OTHER), "Nurse", descriptionUpdate, strengthUpdate, statusUpdate, createNeedListUpdate(), null);

        assertThat(step.getOwner()).hasSize(1);
        assertThat(step.getOwner()).contains(ActionOwner.OTHER);
        assertThat(step.getOwnerOther()).isEqualTo("Nurse");
    }

    @Test
    public void shouldUpdateStepWithOtherOwnerMultiple() {
        var step = createValidStep();
        step.updateStep(List.of(ActionOwner.PRACTITIONER, ActionOwner.OTHER), "Nurse", descriptionUpdate, strengthUpdate, statusUpdate, createNeedListUpdate(), null);

        assertThat(step.getOwner()).hasSize(2);
        assertThat(step.getOwner()).contains(ActionOwner.PRACTITIONER);
        assertThat(step.getOwner()).contains(ActionOwner.OTHER);
        assertThat(step.getOwnerOther()).isEqualTo("Nurse");
    }

    @Test(expected = ValidationException.class)
    public void shouldNotUpdateStepWithOtherOwnerNull() {
        var step = createValidStep();
        step.updateStep(List.of(ActionOwner.OTHER), null, descriptionUpdate, strengthUpdate, statusUpdate, null, null);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotUpdateStepWithNoDescriptionAndIntervention() {
        var step = createValidStep();
        step.updateStep(ownerUpdate, null, "", strengthUpdate, statusUpdate, null, "");
    }

    @Test
    public void shouldUpdateStepWithDescriptionNoIntervention() {
        var step = createValidStep();
        step.updateStep(ownerUpdate, null, descriptionUpdate, strengthUpdate, statusUpdate, createNeedListUpdate(), null);

        assertThat(step.getDescription()).isEqualTo(descriptionUpdate);
        assertThat(step.getIntervention()).isEqualTo(null);
    }

    @Test
    public void shouldUpdateStepWithDescriptionAndIntervention() {
        var step = createValidStep();
        step.updateStep(ownerUpdate, null, descriptionUpdate, strengthUpdate, statusUpdate, createNeedListUpdate(), interventionUpdate);

        assertThat(step.getDescription()).isEqualTo(interventionUpdate);
        assertThat(step.getIntervention()).isEqualTo(interventionUpdate);
    }

    @Test
    public void shouldUpdateStepWithNoDescriptionButAnIntervention() {
        var step = createValidStep();
        step.updateStep(ownerUpdate, null, null, strengthUpdate, statusUpdate, createNeedListUpdate(), interventionUpdate);

        assertThat(step.getDescription()).isEqualTo(interventionUpdate);
        assertThat(step.getIntervention()).isEqualTo(interventionUpdate);
    }

    @Test
    public void shouldUpdateStepPriority() {

        var step = createValidStep();

        assertThat(step.getPriority()).isEqualTo(0);

        step.setPriority(5);

        assertThat(step.getPriority()).isEqualTo(5);
    }

    @Test
    public void shouldAddProgress() {

        var step = createValidStep();

        assertThat(step.getStatus()).isEqualTo(status);

        var newProgress = new ProgressEntity(ActionStatus.COMPLETED, "", LocalDateTime.now(), "");
        step.addProgress(newProgress);

        assertThat(step.getStatus()).isEqualTo(ActionStatus.COMPLETED);
        assertThat(step.getLatestUpdated()).isEqualTo(newProgress.getCreated());
        assertThat(step.getProgress()).hasSize(1);
    }

    private static ActionEntity createValidStep() {
        return new ActionEntity(owner, null, description, strength, status, createNeedList() , null);
    }

    private static List<UUID> createNeedList(){
        return List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }

    private static List<UUID> createNeedListUpdate(){
        return List.of(UUID.randomUUID(), UUID.randomUUID());
    }
}