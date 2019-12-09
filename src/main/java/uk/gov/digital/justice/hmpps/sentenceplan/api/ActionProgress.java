package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.ProgressEntity;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Progress logged against an action")
public class ActionProgress {

    @JsonProperty("status")
    private ActionStatus status;

    @JsonProperty("targetDate")
    private YearMonth targetDate;


    @JsonProperty("motivationUUID")
    private UUID motivationUUID;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("createdBy")
    private String createdBy;


    public static ActionProgress from(ProgressEntity progressEntity) {
        return new ActionProgress(progressEntity.getStatus(), progressEntity.getTargetDate(),
                progressEntity.getMotivationUUID(), progressEntity.getComment(),
                progressEntity.getCreated(), progressEntity.getCreatedBy());
    }

    public static List<ActionProgress> from(List<ProgressEntity> progressEntities) {
        return progressEntities.stream().map(ActionProgress::from).collect(Collectors.toList());
    }
}
