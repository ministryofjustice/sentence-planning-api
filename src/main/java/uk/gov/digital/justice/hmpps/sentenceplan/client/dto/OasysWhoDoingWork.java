package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OasysWhoDoingWork {
    private String code;
    private String description;
    private String comments;
}
