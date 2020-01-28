package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.ActionEntity;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Action on a Sentence Plan Objective")
public class ActionDto {

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
    private UUID intervention;
    @JsonProperty("motivationUUID")
    private UUID motivation;
    @JsonProperty("priority")
    private Integer priority;
    @JsonProperty("updated")
    private LocalDateTime updated;
    @JsonProperty("targetDate")
    private YearMonth targetDate;
    @JsonProperty("progress")
    private List<ActionProgress> progress;

    public static ActionDto from(ActionEntity action) {
        return new ActionDto(action.getId(),
                action.getOwner(),
                action.getOwnerOther(),
                action.getDescription(),
                action.getStatus(),
                action.getInterventionUUID(),
                action.getMotivationUUID(),
                action.getPriority(),
                action.getUpdated(),
                action.getTargetDate(),
                action.getProgress().stream().map(ActionProgress::from).collect(Collectors.toList()));
    }

    public static List<ActionDto> from(Collection<ActionEntity> actions) {
        return actions.stream().map(ActionDto::from).collect(Collectors.toList());
    }
}
