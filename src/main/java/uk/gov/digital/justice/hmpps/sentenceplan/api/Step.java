package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.StepEntity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Step {

    @JsonProperty("owner")
    private StepOwner owner;
    @JsonProperty("ownerOther")
    private String ownerOther;
    @JsonProperty("description")
    private String description;
    @JsonProperty("strength")
    private String strength;
    @JsonProperty("status")
    private StepStatus status;
    @JsonProperty("needs")
    private List<UUID> needs;
    @JsonProperty("interventions")
    private Map<String,String> interventions;

    public static Step from(StepEntity step) {
        return new Step(step.getOwner(),
                step.getOwnerOther(),
                step.getDescription(),
                step.getStrength(),
                step.getStatus(),
                step.getNeeds(),
                step.getInterventions());
    }

    public static List<Step> from(List<StepEntity> steps) {
        return steps.stream().map(Step::from).collect(Collectors.toList());
    }
}
