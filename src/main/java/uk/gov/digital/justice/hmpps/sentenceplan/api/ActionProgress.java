package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.ProgressEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ActionProgress {

    @JsonProperty("status")
    private ActionStatus status;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("createdBy")
    private String createdBy;


    public static ActionProgress from(ProgressEntity progressEntity) {
        return new ActionProgress(progressEntity.getStatus(), progressEntity.getCreated(), progressEntity.getCreatedBy());
    }

    public static List<ActionProgress> from(List<ProgressEntity> progressEntities) {
        return progressEntities.stream().map(ActionProgress::from).collect(Collectors.toList());
    }
}
