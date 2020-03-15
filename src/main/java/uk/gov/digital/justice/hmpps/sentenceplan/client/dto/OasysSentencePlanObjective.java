package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OasysSentencePlanObjective {

    private List<OasysCriminogenicNeed> criminogenicNeeds;
    private List<OasysIntervention> interventions;
    private OasysObjectiveMeasure objectiveMeasure;
    private OasysRefElement objectiveType;
    private String objectiveCode;
    private String objectiveDescription;
    private String howMeasured;
    private String objectiveHeading;
    private String objectiveComment;
    private LocalDateTime createdDate;
}
