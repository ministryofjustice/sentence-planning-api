package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.ActionEntity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Action {

    @JsonProperty("owner")
    private ActionOwner owner;
    @JsonProperty("ownerOther")
    private String ownerOther;
    @JsonProperty("description")
    private String description;
    @JsonProperty("strength")
    private String strength;
    @JsonProperty("status")
    private ActionStatus status;
    @JsonProperty("needs")
    private List<UUID> needs;
    @JsonProperty("interventions")
    private Map<String,String> interventions;

    public static Action from(ActionEntity action) {
        return new Action(action.getOwner(),
                action.getOwnerOther(),
                action.getDescription(),
                action.getStrength(),
                action.getStatus(),
                action.getNeeds(),
                action.getInterventions());
    }

    public static List<Action> from(List<ActionEntity> actions) {
        return actions.stream().map(Action::from).collect(Collectors.toList());
    }
}
