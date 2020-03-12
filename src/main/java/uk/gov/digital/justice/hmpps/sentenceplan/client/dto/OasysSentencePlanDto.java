package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class OasysSentencePlanDto {
    private Long oasysSetId;
    private LocalDate createdDate;
    private LocalDate completedDate;
    private List<OasysSentencePlanObjective> objectives;
    private Map<String, OasysQuestion> questions;
}
