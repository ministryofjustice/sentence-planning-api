package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanPropertiesEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    @JsonProperty("actions")
    private List<Action> actions;
    @JsonProperty("needs")
    private List<Need> needs;
    @JsonProperty("comments")
    private Map<CommentType, Comment> comments;
    @JsonProperty("childSafeguardingIndicated")
    private Boolean childSafeguardingIndicated;
    @JsonProperty("complyWithChildProtectionPlanIndicated")
    private Boolean complyWithChildProtectionPlanIndicated;
    @JsonProperty("offender")
    private Offender offender;


    public static SentencePlan from(SentencePlanEntity sentencePlan) {

        var data = Optional.ofNullable(sentencePlan.getData()).orElseGet(SentencePlanPropertiesEntity::new);

        var offenderEntity = Optional.ofNullable(sentencePlan.getOffender()).orElseGet(OffenderEntity::new);

        return new SentencePlan(sentencePlan.getUuid(), sentencePlan.getCreatedOn(), sentencePlan.getStatus(),
                Action.from(data.getActions(), sentencePlan.getNeeds()), Need.from(sentencePlan.getNeeds()),
                Comment.from(data.getComments()),
                data.getChildSafeguardingIndicated(), data.getComplyWithChildProtectionPlanIndicated(),
                Offender.from(offenderEntity));
    }
}
