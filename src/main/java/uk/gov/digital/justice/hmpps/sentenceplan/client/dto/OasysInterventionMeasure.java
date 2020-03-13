package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;

@Builder(access = AccessLevel.PRIVATE)
@Value
public class OasysInterventionMeasure {
    private String comments;
    private OasysRefElement status;


}
