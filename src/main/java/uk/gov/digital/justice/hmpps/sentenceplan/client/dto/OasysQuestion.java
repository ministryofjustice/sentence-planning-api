package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OasysQuestion {
    private Long refQuestionId;
    private String refQuestionCode;
    private Long oasysQuestionId;
    private Long displayOrder;
    private Long displayScore;
    private String questionText;
    private OasysAnswer answer;
}
