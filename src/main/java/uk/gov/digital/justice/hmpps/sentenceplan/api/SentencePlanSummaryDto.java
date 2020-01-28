package uk.gov.digital.justice.hmpps.sentenceplan.api;

import io.swagger.annotations.ApiModel;
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
@ApiModel(description = "Sentence Plan Summary model")
public class SentencePlanSummaryDto {
    private String planId;
    private LocalDate createdDate;
    private LocalDate completedDate;
    private boolean legacy;
    private boolean isDraft;

    public SentencePlanSummaryDto(String id, LocalDateTime createdDate, LocalDateTime completedDate, boolean legacy, boolean isDraft) {
        this.planId = id;
        this.createdDate = createdDate == null ? null : createdDate.toLocalDate();
        this.completedDate = completedDate == null ? null : completedDate.toLocalDate();
        this.legacy = legacy;
        this.isDraft = isDraft;
    }
}
