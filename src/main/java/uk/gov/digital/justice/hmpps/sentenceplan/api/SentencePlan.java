package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanPropertiesEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SentencePlan {
    @JsonProperty("id")
    private UUID uuid;
    @JsonProperty("createdOn")
    private LocalDateTime createdOn;
    @JsonProperty("status")
    private PlanStatus status;
    @JsonProperty("steps")
    private List<Step> steps;
    @JsonProperty("needs")
    private List<Need> needs;
    @JsonProperty("serviceUserComments")
    private String serviceUserComments;
    @JsonProperty("practitionerComments")
    private String practitionerComments;
    @JsonProperty("childSafeguardingIndicated")
    private Boolean childSafeguardingIndicated;
    @JsonProperty("complyWithChildProtectionPlanIndicated")
    private Boolean complyWithChildProtectionPlanIndicated;

    public static SentencePlan from(SentencePlanEntity sentencePlan) {

        var data = Optional.ofNullable(sentencePlan.getData()).orElseGet(() -> new SentencePlanPropertiesEntity());

        return new SentencePlan(sentencePlan.getUuid(), sentencePlan.getCreatedOn(), sentencePlan.getStatus(),
                Step.from(data.getSteps()), Need.from(sentencePlan.getNeeds()),
                data.getServiceUserComments(), data.getPractitionerComments(),
                data.getChildSafeguardingIndicated(), data.getComplyWithChildProtectionPlanIndicated());
    }
}
