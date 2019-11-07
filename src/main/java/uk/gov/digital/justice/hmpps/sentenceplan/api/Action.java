package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.ActionEntity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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
    @JsonProperty("status")
    private ActionStatus status;
    @JsonProperty("intervention")
    private String intervention;
    @JsonProperty("priority")
    private Integer priority;
    @JsonProperty("updated")
    private LocalDateTime updated;

    public static Action from(ActionEntity action) {
        return new Action(action.getId(),
                action.getOwner(),
                action.getOwnerOther(),
                action.getDescription(),
                action.getStatus(),
                action.getIntervention(),
                action.getPriority(),
                action.getUpdated());
    }

    public static List<Action> from(Collection<ActionEntity> actions) {
        return actions.stream().map(Action::from).collect(Collectors.toList());
    }
}
