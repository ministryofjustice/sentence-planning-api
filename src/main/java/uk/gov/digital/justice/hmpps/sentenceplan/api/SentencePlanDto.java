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
public class SentencePlanDto {
    @JsonProperty("id")
    private UUID uuid;
    @JsonProperty("objectives")
    private List<ObjectiveDto> objectives;
    @JsonProperty("needs")
    private List<NeedDto> needs;
    @JsonProperty("comments")
    private List<CommentDto> comments;
    @JsonProperty("childSafeguardingIndicated")
    private Boolean childSafeguardingIndicated;
    @JsonProperty("offender")
    private Offender offender;
    @JsonProperty("createdDate")
    private LocalDateTime createdOn;
    @JsonProperty("draft")
    private boolean draft;

    public static SentencePlanDto from(SentencePlanEntity sentencePlan) {

        var data = Optional.ofNullable(sentencePlan.getData()).orElseGet(SentencePlanPropertiesEntity::new);

        var offenderEntity = Optional.ofNullable(sentencePlan.getOffender()).orElseGet(OffenderEntity::new);

        return new SentencePlanDto(sentencePlan.getUuid(),
                                ObjectiveDto.from(data.getObjectives().values()),
                                NeedDto.from(sentencePlan.getNeeds()),
                                CommentDto.from(data.getComments().values()),
                                data.getChildSafeguardingIndicated(),
                                Offender.from(offenderEntity),
                                sentencePlan.getCreatedOn(),
                                sentencePlan.isDraft());
    }
}
