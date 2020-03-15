package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OasysAssessment {
    @JsonProperty("assessmentId")
    private long assessmentId;

    @JsonProperty("assessmentStatus")
    private String assessmentStatus;

    @JsonProperty("layer3SentencePlanNeeds")
    private List<AssessmentNeed> needs;

    @JsonProperty("childSafeguardingIndicated")
    private Boolean childSafeguardingIndicated;

}
