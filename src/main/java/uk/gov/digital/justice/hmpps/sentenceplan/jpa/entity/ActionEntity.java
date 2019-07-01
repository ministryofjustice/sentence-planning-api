package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.*;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class ActionEntity implements Serializable {

    private ActionOwner owner;
    private String ownerOther;
    private String description;
    private String strength;
    private ActionStatus status;
    private List<UUID> needs;
    private Map<String, String> interventions;

    public ActionEntity() {
        interventions = new HashMap<>();
    }

    public ActionEntity(ActionOwner owner, String ownerOther, String description, String strength, ActionStatus status, List<UUID> needs, Map<String, String> interventions) {
        this.owner = owner;
        this.ownerOther = ownerOther;
        this.description = description;
        this.strength = strength;
        this.status = status;
        this.needs = needs;
        this.interventions = interventions;
    }
}
