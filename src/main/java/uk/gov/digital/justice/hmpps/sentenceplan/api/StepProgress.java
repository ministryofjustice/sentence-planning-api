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
public class StepProgress {

    @JsonProperty("status")
    private StepStatus status;

    @JsonProperty("comments")
    private String practitionerComments;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("createdBy")
    private String createdBy;


    public static StepProgress from(ProgressEntity progressEntity) {
        return new StepProgress(progressEntity.getStatus(), progressEntity.getPractitionerComments(), progressEntity.getCreated(), progressEntity.getCreatedBy());
    }

    public static List<StepProgress> from(List<ProgressEntity> progressEntities) {
        return progressEntities.stream().map(StepProgress::from).collect(Collectors.toList());
    }
}
