package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OasysAnswer {
    private Long refAnswerId;
    private String refAnswerCode;
    private Long oasysAnswerId;
    private Long displayOrder;
    private String staticText;
    private String freeFormText;
    private Long ogpScore;
    private Long ovpScore;
    private Long qaRawScore;
}

