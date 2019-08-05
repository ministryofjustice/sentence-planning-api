package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.*;
import org.springframework.util.StringUtils;
import uk.gov.digital.justice.hmpps.sentenceplan.api.StepOwner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.StepStatus;
import uk.gov.digital.justice.hmpps.sentenceplan.application.ValidationException;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StepEntity implements Serializable {

    private UUID id;
    private StepOwner owner;
    private String ownerOther;
    private String description;
    private String strength;
    private StepStatus status;
    private List<UUID> needs;
    private String intervention;


    public StepEntity(StepOwner owner, String ownerOther, String description, String strength, StepStatus status, List<UUID> needs, String intervention) {

        updateStep(owner, ownerOther, description, strength, status, needs, intervention);
        this.id = UUID.randomUUID();
    }

    public void updateStep(StepOwner owner, String ownerOther, String description, String strength, StepStatus status, List<UUID> needs, String intervention) {

        validateNeeds(needs);
        validateOwner(owner, ownerOther);
        validateDescription(description, intervention);

        this.owner = owner;
        this.ownerOther = ownerOther;

        // Overwrite the description
        if(StringUtils.isEmpty(intervention)) {
            this.description = description;
            this.intervention = intervention;
        } else {
            this.description = intervention;
            this.intervention = intervention;
        }

        this.strength = strength;
        this.status = status;

        // When we update a step we just overwrite whatever needs there are, we don't try to merge/deduplicate the list
        this.needs = needs;
    }

    private void validateNeeds(List<UUID> needs) {
        if(needs == null || needs.size() < 1) {
            throw new ValidationException("Step must address one or more needs");
        }
    }

    private void validateOwner(StepOwner owner, String ownerOther) {
        if(owner == null) {
            throw new ValidationException("Owner must be specified");
        }
        if(owner.equals(StepOwner.OTHER) && StringUtils.isEmpty(ownerOther)) {
            throw new ValidationException("OwnerOther must be specified if StepOwner is OTHER");
        }
    }

    private void validateDescription(String description, String intervention) {
        if(StringUtils.isEmpty(intervention) && StringUtils.isEmpty(description)){
            throw new ValidationException("Description must be specified if intervention is not specified");
        }
    }
}
