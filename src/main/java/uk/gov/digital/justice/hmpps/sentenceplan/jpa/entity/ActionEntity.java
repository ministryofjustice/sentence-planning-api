package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.util.StringUtils;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus;
import uk.gov.digital.justice.hmpps.sentenceplan.application.ValidationException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@AllArgsConstructor
public class ActionEntity implements Serializable {

    private UUID id;

    private List<ActionOwner> owner;

    private String ownerOther;

    private String description;

    private String strength;

    private ActionStatus status;

    private List<UUID> needs;

    private String intervention;

    private int priority;

    private List<ProgressEntity> progress;

    private LocalDateTime created;

    private LocalDateTime updated;


    public ActionEntity() {
        this.progress = new ArrayList<>(0);
    }

    public ActionEntity(List<ActionOwner> owner, String ownerOther, String description, String strength, ActionStatus status, List<UUID> needs, String intervention) {
        var now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.owner = new ArrayList<>(0);
        this.progress = new ArrayList<>(0);
        this.created = now;
        this.updated = now;
        update(owner, ownerOther, description, strength, status, needs, intervention);
    }

    public void updateStep(List<ActionOwner> owner, String ownerOther, String description, String strength, ActionStatus status, List<UUID> needs, String intervention) {
        update(owner, ownerOther, description, strength, status, needs, intervention);
        this.updated = LocalDateTime.now();
    }

    @JsonIgnore
    public LocalDateTime getLatestUpdated() {
        Optional<LocalDateTime> lastProgressed = this.progress.stream().map(ProgressEntity::getCreated).max(Comparator.naturalOrder());

        if(lastProgressed.isPresent()) {
            return this.updated.isBefore(lastProgressed.get()) ? lastProgressed.get() : this.updated ;
        } else {
            return this.updated;
        }
    }

    public void addProgress(ProgressEntity progressEntity) {
        this.progress.add(progressEntity);
        this.status = progressEntity.getStatus();
    }

    private void update(List<ActionOwner> owner, String ownerOther, String description, String strength, ActionStatus status, List<UUID> needs, String intervention) {
        validateOwner(owner, ownerOther);
        validateNeeds(needs);
        validateDescription(description, intervention);

        // Overwrite the description if there is an intervention.
        this.description = StringUtils.isEmpty(intervention) ? description : intervention;

        this.intervention = intervention;

        this.strength = strength;
        this.status = status;

        // When we update a step we just overwrite whatever needs and owners there are, we don't try to merge/deduplicate the list
        this.needs = needs;
        this.owner = owner;
        this.ownerOther = ownerOther;
    }

    public static ActionEntity updatePriority(ActionEntity actionEntity, int priority) {
        actionEntity.setPriority(priority);
        return actionEntity;
    }

    private void validateNeeds(List<UUID> needs) {
        if(needs == null || needs.isEmpty()) {
            throw new ValidationException("Action must address one or more needs");
        }
    }

    private void validateOwner(List<ActionOwner> owner, String ownerOther) {
        if(owner == null || owner.isEmpty()) {
            throw new ValidationException("Owner must be specified");
        }

        if(owner.contains(ActionOwner.OTHER) && StringUtils.isEmpty(ownerOther)) {
            throw new ValidationException("OwnerOther must be specified if ActionOwner is OTHER");
        }
    }

    private void validateDescription(String description, String intervention) {
        if(StringUtils.isEmpty(intervention) && StringUtils.isEmpty(description)){
            throw new ValidationException("Description must be specified if intervention is not specified");
        }
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
