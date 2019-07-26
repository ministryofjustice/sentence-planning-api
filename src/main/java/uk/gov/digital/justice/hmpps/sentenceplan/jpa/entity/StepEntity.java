package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.*;
import uk.gov.digital.justice.hmpps.sentenceplan.api.StepOwner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.StepStatus;
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

        this.id = UUID.randomUUID();
        this.owner = owner;
        this.ownerOther = ownerOther;
        this.description = description;
        this.strength = strength;
        this.status = status;
        this.needs = needs;
        this.intervention = intervention;
    }
}
