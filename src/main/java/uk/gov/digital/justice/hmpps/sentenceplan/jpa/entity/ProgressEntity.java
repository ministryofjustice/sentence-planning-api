package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgressEntity implements Serializable {

    private ActionStatus status;
    private YearMonth targetDate;
    private UUID motivationUUID;
    private String comment;
    private LocalDateTime created = LocalDateTime.now();
    private String createdBy;

    public ProgressEntity(ActionStatus actionStatus, YearMonth targetDate, UUID motivationUUID, String comment, String createdBy) {
        this.status = actionStatus;
        this.targetDate = targetDate;
        this.motivationUUID = motivationUUID;
        this.comment = comment;
        this.createdBy = createdBy;
    }
}
