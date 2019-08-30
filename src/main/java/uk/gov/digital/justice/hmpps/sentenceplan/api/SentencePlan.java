package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanPropertiesEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
    @JsonProperty("comments")
    private List<Comment> comments;
    @JsonProperty("serviceUserComments")
    private String serviceUserComments;
    @JsonProperty("childSafeguardingIndicated")
    private Boolean childSafeguardingIndicated;
    @JsonProperty("complyWithChildProtectionPlanIndicated")
    private Boolean complyWithChildProtectionPlanIndicated;
    @JsonProperty("bookingNumber")
    private Long bookingNumber;

    public static SentencePlan from(SentencePlanEntity sentencePlan) {

        var data = Optional.ofNullable(sentencePlan.getData()).orElseGet(SentencePlanPropertiesEntity::new);

        return new SentencePlan(sentencePlan.getUuid(), sentencePlan.getCreatedOn(), sentencePlan.getStatus(),
                Step.from(data.getSteps(), sentencePlan.getNeeds()), Need.from(sentencePlan.getNeeds()),
                Comment.from(data.getComments()),
                data.getServiceUserComments(),
                data.getChildSafeguardingIndicated(), data.getComplyWithChildProtectionPlanIndicated(),
                Objects.nonNull(sentencePlan.getOffender()) ? sentencePlan.getOffender().getBookingNumber() : null);
    }
}
