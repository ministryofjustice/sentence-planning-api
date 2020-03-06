package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.ObjectiveStatusEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.ProgressEntity;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Objective status change")
public class ObjectiveStatusChange {

    @JsonProperty("status")
    private ObjectiveStatus status;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("createdBy")
    private String createdBy;


    public static ObjectiveStatusChange from(ObjectiveStatusEntity statusChange) {
        return new ObjectiveStatusChange(statusChange.getStatus(),statusChange.getComment(),
                statusChange.getCreated(), statusChange.getCreatedBy());
    }

    public static List<ObjectiveStatusChange> from(List<ObjectiveStatusEntity> statusChanges) {
        return statusChanges.stream().map(ObjectiveStatusChange::from).collect(Collectors.toList());
    }
}
