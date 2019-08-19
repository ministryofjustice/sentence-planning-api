package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OasysCriminogenicNeed {
    private String code;
    private String description;
    private Integer priority;
}
