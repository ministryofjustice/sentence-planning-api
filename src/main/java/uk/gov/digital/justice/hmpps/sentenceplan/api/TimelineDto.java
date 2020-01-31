package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.*;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityCreationException;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Timeline")
public class TimelineDto {

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("eventType")
    private String type;

    @JsonProperty("timelineType")
    private String timelineType;

    @JsonProperty("comment")
    private CommentDto comment;

    @JsonProperty("objective")
    private ObjectiveDto objective;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public static TimelineDto from(TimelineEntity timelineEntity, ObjectMapper objectMapper) {
        CommentDto commentDto = null;
        ObjectiveDto objectiveDto = null;
        try {
            if (timelineEntity.getComment() != null) {
               commentDto = objectMapper.readValue(timelineEntity.getComment(), CommentDto.class);
            }
            if (timelineEntity.getObjective() != null) {
                objectiveDto = objectMapper.readValue(timelineEntity.getObjective(), ObjectiveDto.class);
            }
        } catch (java.io.IOException e) {
            throw new EntityCreationException("Cannot construct TimelineDto");
        }

        return new TimelineDto(timelineEntity.getUserId(), timelineEntity.getType().toString(), calculateType(timelineEntity).name(), commentDto, objectiveDto, timelineEntity.getEventTimestamp());
    }

    public static List<TimelineDto> from(Collection<TimelineEntity> timelineEntities, ObjectMapper objectMapper) {
        return timelineEntities.stream().map(t -> TimelineDto.from(t, objectMapper)).collect(Collectors.toList());
    }

    private static TimelineType calculateType(TimelineEntity timelineEntity){
        if(timelineEntity.getObjective() == null && timelineEntity.getComment() == null) {
            return TimelineType.PLAN;
        }
        if(timelineEntity.getComment() != null) {
            return TimelineType.COMMENT;
        }
        if(timelineEntity.getObjective() != null) {
            return TimelineType.OBJECTIVE;
        }
        return TimelineType.PLAN;
    }

}
