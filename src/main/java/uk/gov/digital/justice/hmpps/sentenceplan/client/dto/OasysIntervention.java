package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OasysIntervention {
    private Boolean copiedForward;
    private String interventionComment;
    private OasysRefElement timescale;
    private String interventionCode;
    private String interventionDescription;
}
