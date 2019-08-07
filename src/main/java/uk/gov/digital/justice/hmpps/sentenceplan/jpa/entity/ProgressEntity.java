package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.api.StepOwner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.StepStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgressEntity implements Serializable {

    private StepStatus status;
    private String practitionerComments;
    private LocalDateTime created;
    private String createdBy;

    public ProgressEntity(StepStatus stepStatus, String practitionerComments, String createdBy) {
        this.status = stepStatus;
        this.practitionerComments = practitionerComments;
        this.created = LocalDateTime.now();
        this.createdBy = createdBy;
    }
}
