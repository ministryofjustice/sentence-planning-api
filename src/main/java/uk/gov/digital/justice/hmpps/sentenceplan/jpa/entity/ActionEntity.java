package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

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
@NoArgsConstructor
public class ActionEntity implements Serializable {

    private UUID id;

    private List<ActionOwner> owner = new ArrayList<>(0);

    private String ownerOther;

    private String description;

    private String strength;

    private ActionStatus status;

    private List<UUID> needs = new ArrayList<>(0);

    private String intervention;

    private int priority;

    private List<ProgressEntity> progress = new ArrayList<>(0);

    private LocalDateTime created = LocalDateTime.now();

    private LocalDateTime updated;

    public ActionEntity(List<ActionOwner> owner, String ownerOther, String description, String strength, ActionStatus status, List<UUID> needs, String intervention) {
        this.id = UUID.randomUUID();
        update(owner, ownerOther, description, strength, status, needs, intervention);
        this.updated = this.created;
    }

    public void updateAction(List<ActionOwner> owner, String ownerOther, String description, String strength, ActionStatus status, List<UUID> needs, String intervention) {
        update(owner, ownerOther, description, strength, status, needs, intervention);
        this.updated = LocalDateTime.now();
    }

    public void addProgress(ProgressEntity progressEntity) {
        this.status = progressEntity.getStatus();
        this.updated = progressEntity.getCreated();
        this.progress.add(progressEntity);
    }

    public void setPriority(int priority) {
        this.priority = priority;
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

        // When we update a action we just overwrite whatever needs and owners there are, we don't try to merge/deduplicate the list
        this.needs = needs;
        this.owner = owner;
        this.ownerOther = ownerOther;
    }

    private static void validateNeeds(List<UUID> needs) {
        if(needs == null || needs.isEmpty()) {
            throw new ValidationException("Action must address one or more needs");
        }
    }

    private static void validateOwner(List<ActionOwner> owner, String ownerOther) {
        if(owner == null || owner.isEmpty()) {
            throw new ValidationException("Owner must be specified");
        }

        if(owner.contains(ActionOwner.OTHER) && StringUtils.isEmpty(ownerOther)) {
            throw new ValidationException("OwnerOther must be specified if ActionOwner is OTHER");
        }
    }

    private static void validateDescription(String description, String intervention) {
        if(StringUtils.isEmpty(intervention) && StringUtils.isEmpty(description)){
            throw new ValidationException("Description must be specified if intervention is not specified");
        }
    }

}
