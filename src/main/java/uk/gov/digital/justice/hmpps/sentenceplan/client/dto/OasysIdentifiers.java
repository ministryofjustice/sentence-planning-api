package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OasysIdentifiers {

    @JsonProperty("nomisId")
    private String nomisId;

}