package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OasysAuthorisationDto {
    @JsonProperty("oasysUserCode")
    private String oasysUserCode;
    @JsonProperty("oasysOffenderId")
    private Long oasysOffenderId;
    @JsonProperty("oasysOffenderPermissionLevel")
    private OasysOffenderPermissionLevel oasysOffenderPermissionLevel;
    @JsonProperty("oasysOffenderPermissionResource")
    private OasysOffenderPermissionResource oasysOffenderPermissionResource;
}

