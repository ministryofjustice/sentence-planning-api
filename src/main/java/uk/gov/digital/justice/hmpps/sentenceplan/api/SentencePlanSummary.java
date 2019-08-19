package uk.gov.digital.justice.hmpps.sentenceplan.api;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class SentencePlanSummary {
    private String planId;
    private LocalDate createdDate;
    private LocalDate completedDate;
    private boolean legacy;

    public SentencePlanSummary(String id, LocalDateTime createdDate, LocalDateTime completedDate, boolean legacy) {
        this.planId = id;
        this.createdDate = createdDate == null ? null : createdDate.toLocalDate();
        this.completedDate = completedDate == null ? null : completedDate.toLocalDate();
        this.legacy = legacy;
    }
}
