package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.*;
import uk.gov.digital.justice.hmpps.sentenceplan.api.StepOwner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.StepStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class StepEntity implements Serializable {

    private StepOwner owner;
    private String ownerOther;
    private String description;
    private String strength;
    private StepStatus status;
    private List<UUID> needs;
    private Map<String, String> interventions;

    public StepEntity() {
        interventions = new HashMap<>();
    }

    public StepEntity(StepOwner owner, String ownerOther, String description, String strength, StepStatus status, List<UUID> needs, Map<String, String> interventions) {
        this.owner = owner;
        this.ownerOther = ownerOther;
        this.description = description;
        this.strength = strength;
        this.status = status;
        this.needs = needs;
        this.interventions = interventions;
    }
}
