package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OasysOffender {
    @JsonProperty("oasysOffenderId")
    private Long oasysOffenderId;
    @JsonProperty("limitedAccessOffender")
    private boolean limitedAccessOffender;
    @JsonProperty("familyName")
    private String familyName;
    @JsonProperty("forename1")
    private String forename1;
    @JsonProperty("forename2")
    private String forename2;
    @JsonProperty("forename3")
    private String forename3;
    @JsonProperty("riskToOthers")
    private String riskToOthers;
    @JsonProperty("riskToSelf")
    private String riskToSelf;
    @JsonProperty("pnc")
    private String pnc;
    @JsonProperty("crn")
    private String crn;
    @JsonProperty("nomisId")
    private String nomisId;
    @JsonProperty("legacyCmsProbNumber")
    private String legacyCmsProbNumber;
    @JsonProperty("croNumber")
    private String croNumber;
    @JsonProperty("bookingNumber")
    private String bookingNumber;
    @JsonProperty("mergePncNumber")
    private String mergePncNumber;
    @JsonProperty("mergedOasysOffenderId")
    private Long mergedOasysOffenderId;
}