package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.ObjectiveEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Objective {

    @JsonProperty("id")
    private UUID id;
    @JsonProperty("description")
    private String description;
    @JsonProperty("needs")
    private List<UUID> needs;
    @JsonProperty("actions")
    private List<Action> actions;
    @JsonProperty("priority")
    private Integer priority;

    public static Objective from(ObjectiveEntity objective) {
        var actions = Action.from(objective.getActions().values());
        return new Objective(objective.getId(), objective.getDescription(), objective.getNeeds(), actions, objective.getPriority());
    }

    public static List<Objective> from(Collection<ObjectiveEntity> objectives) {
        return objectives.stream().map(Objective::from).collect(Collectors.toList());
    }
}
