package uk.gov.digital.justice.hmpps.sentenceplan.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class SentencePlan {
    private LocalDateTime createdOn;
}
