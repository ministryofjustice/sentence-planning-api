package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.application.LocalDateTimeAttributeConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "NEED")
public class NeedEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "UUID")
    private UUID uuid;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "OVER_THRESHOLD")
    private Boolean overThreshold;

    @Column(name = "REOFFENDING_RISK")
    private Boolean reoffendingRisk;

    @Column(name = "HARM_RISK")
    private Boolean harmRisk;

    @Column(name = "LOW_SCORE_RISK")
    private Boolean lowScoreRisk;

    @Column(name = "ACTIVE")
    private Boolean active;

    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;

    @ManyToOne
    @JoinColumn(name = "SENTENCE_PLAN_UUID", referencedColumnName = "UUID")
    private SentencePlanEntity sentencePlan;


    public NeedEntity(String description, Boolean overThreshold, Boolean reoffendingRisk, Boolean harmRisk, Boolean lowScoreRisk, Boolean active, SentencePlanEntity sentencePlan) {
        this.uuid = UUID.randomUUID();
        this.description = description;
        this.overThreshold = overThreshold;
        this.reoffendingRisk = reoffendingRisk;
        this.harmRisk = harmRisk;
        this.lowScoreRisk = lowScoreRisk;
        this.active = active;
        this.createdOn = LocalDateTime.now();
        this.sentencePlan = sentencePlan;
    }
}
