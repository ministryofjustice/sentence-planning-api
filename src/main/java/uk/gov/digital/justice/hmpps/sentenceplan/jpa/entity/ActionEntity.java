package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.*;
import org.springframework.util.StringUtils;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus;
import uk.gov.digital.justice.hmpps.sentenceplan.application.ValidationException;

import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ActionEntity implements Serializable {

    private UUID id;

    private UUID interventionUUID;

    private String description;

    private YearMonth targetDate;

    private UUID motivationUUID;

    private List<ActionOwner> owner = new ArrayList<>(0);

    private String ownerOther;

    private ActionStatus status;

    private int priority;

    private List<ProgressEntity> progress = new ArrayList<>(0);

    private LocalDateTime created = LocalDateTime.now();

    private LocalDateTime updated;

    @Transient
    @Setter
    private String intervention;

    public ActionEntity(UUID interventionUUID, String description, YearMonth targetDate, UUID motivationUUID, List<ActionOwner> owner, String ownerOther, ActionStatus status) {
        this.id = UUID.randomUUID();
        update(interventionUUID, description, targetDate, motivationUUID, owner, ownerOther, status);
    }

    public void updateAction(UUID interventionUUID, String description, YearMonth targetDate, UUID motivationUUID, List<ActionOwner> owner, String ownerOther, ActionStatus status) {
        update(interventionUUID, description, targetDate, motivationUUID, owner, ownerOther, status);
    }

    public void addProgress(ProgressEntity progressEntity) {
        this.status = progressEntity.getStatus();
        this.motivationUUID = progressEntity.getMotivationUUID();
        this.updated = progressEntity.getCreated();
        this.progress.add(progressEntity);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    private void setDescriptionIntervention(String description, UUID interventionUUID) {
        if(interventionUUID == null && StringUtils.isEmpty(description)){
            throw new ValidationException("Description must be specified if intervention is not specified");
        }
        this.interventionUUID = interventionUUID;
        this.description =  description;
    }

    private void setOwner(List<ActionOwner> owner, String ownerOther) {
        if(owner == null || owner.isEmpty()) {
            throw new ValidationException("Owner must be specified");
        }

        if(owner.contains(ActionOwner.OTHER) && StringUtils.isEmpty(ownerOther)) {
            throw new ValidationException("OwnerOther must be specified if ActionOwner is OTHER");
        }

        this.owner = owner;
        this.ownerOther = ownerOther;
    }

    private void update(UUID interventionUUID, String description, YearMonth targetDate, UUID motivationUUID, List<ActionOwner> owner, String ownerOther, ActionStatus status) {
        setDescriptionIntervention(description, interventionUUID);
        this.targetDate = targetDate;
        this.motivationUUID = motivationUUID;
        setOwner(owner, ownerOther);
        this.status = status;
    }

}
