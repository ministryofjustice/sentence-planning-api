package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ObjectiveStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectiveStatusEntity implements Serializable {

    private ObjectiveStatus status;
    private String comment;
    private LocalDateTime created = LocalDateTime.now();
    private String createdBy;

    public ObjectiveStatusEntity(ObjectiveStatus objectiveStatus, String comment, String createdBy) {
        this.status = objectiveStatus;
        this.comment = comment;
        this.createdBy = createdBy;
    }
}
