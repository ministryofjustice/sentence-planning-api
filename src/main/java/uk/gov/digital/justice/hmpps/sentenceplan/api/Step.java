package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.NeedEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.StepEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Step {

    @JsonProperty("id")
    private UUID id;
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
    private List<Need> needs;
    @JsonProperty("intervention")
    private String intervention;
    @JsonProperty("priority")
    private Integer priority;
    @JsonProperty("progress")
    private List<StepProgress> progressList;
    @JsonProperty("updated")
    private LocalDateTime updated;

    public static Step from(StepEntity step, List<NeedEntity> needs) {
        return new Step(step.getId(),
                step.getOwner(),
                step.getOwnerOther(),
                step.getDescription(),
                step.getStrength(),
                step.getStatus(),
                Need.from(needs.stream().filter(n-> step.getNeeds().contains(n.getUuid())).collect(Collectors.toList())),
                step.getIntervention(),
                step.getPriority(),
                StepProgress.from(Optional.ofNullable(step.getProgress()).orElse(Collections.emptyList())),
                step.getLatestUpdated());
    }

    public static List<Step> from(List<StepEntity> steps, List<NeedEntity> needs) {
        return steps.stream().map(s-> from(s, needs)).collect(Collectors.toList());
    }
}
