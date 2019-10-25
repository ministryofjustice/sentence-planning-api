package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.NeedEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.ActionEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Action {

    @JsonProperty("id")
    private UUID id;
    @JsonProperty("owner")
    private List<ActionOwner> owner;
    @JsonProperty("ownerOther")
    private String ownerOther;
    @JsonProperty("description")
    private String description;
    @JsonProperty("strength")
    private String strength;
    @JsonProperty("status")
    private ActionStatus status;
    @JsonProperty("needs")
    private List<Need> needs;
    @JsonProperty("intervention")
    private String intervention;
    @JsonProperty("priority")
    private Integer priority;
    @JsonProperty("progress")
    private List<ActionProgress> progressList;
    @JsonProperty("updated")
    private LocalDateTime updated;

    public static Action from(ActionEntity action, List<NeedEntity> needs) {
        return new Action(action.getId(),
                action.getOwner(),
                action.getOwnerOther(),
                action.getDescription(),
                action.getStrength(),
                action.getStatus(),
                Need.from(needs.stream().filter(n-> action.getNeeds().contains(n.getUuid())).collect(Collectors.toList())),
                action.getIntervention(),
                action.getPriority(),
                ActionProgress.from(Optional.ofNullable(action.getProgress()).orElse(Collections.emptyList())),
                action.getLatestUpdated());
    }

    public static List<Action> from(List<ActionEntity> actions, List<NeedEntity> needs) {
        return actions.stream().map(s-> from(s, needs)).collect(Collectors.toList());
    }
}
