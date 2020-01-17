package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanPropertiesEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "The main Sentence Plan Model")
public class SentencePlan {
    @JsonProperty("id")
    private UUID uuid;
    @JsonProperty("objectives")
    private List<Objective> objectives;
    @JsonProperty("needs")
    private List<Need> needs;
    @JsonProperty("comments")
    private List<Comment> comments;
    @JsonProperty("childSafeguardingIndicated")
    private Boolean childSafeguardingIndicated;
    @JsonProperty("offender")
    private Offender offender;
    @JsonProperty("createdDate")
    private LocalDateTime createdOn;
    @JsonProperty("draft")
    private boolean draft;

    public static SentencePlan from(SentencePlanEntity sentencePlan) {

        var data = Optional.ofNullable(sentencePlan.getData()).orElseGet(SentencePlanPropertiesEntity::new);

        var offenderEntity = Optional.ofNullable(sentencePlan.getOffender()).orElseGet(OffenderEntity::new);

        return new SentencePlan(sentencePlan.getUuid(),
                                Objective.from(data.getObjectives().values()),
                                Need.from(sentencePlan.getNeeds()),
                                Comment.from(data.getComments().values()),
                                data.getChildSafeguardingIndicated(),
                                Offender.from(offenderEntity),
                                sentencePlan.getCreatedOn(),
                                sentencePlan.isDraft());
    }
}
