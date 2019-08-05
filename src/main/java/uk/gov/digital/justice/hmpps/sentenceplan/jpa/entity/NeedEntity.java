package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.api.MotivationRef;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @OneToMany(mappedBy = "need", cascade = CascadeType.PERSIST)
    private List<MotivationEntity> motivations = new ArrayList<>();

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

    public List<MotivationEntity> getMotivationHistory() {
        return this.motivations.stream().filter(me -> me.isEnded()).collect(Collectors.toList());
    }

    public Optional<MotivationEntity> getCurrentMotivation() {
        return this.motivations.stream().filter(me -> !me.isEnded()).findFirst();
    }

    /*
There are three scenarios,
1) Adding a first motivationUUID onto a need where we want to add it.
2) Changing a motivationUUID from one to another where we want to end the current one and add it
3) Submitting the same motivationUUID where we don't want to end it or add it again
 */
    public static NeedEntity updateMotivation(NeedEntity needEntity, UUID newMotivationUUID, List<MotivationRefEntity> motivationsRefs) {
        var currentMotivation = needEntity.getCurrentMotivation();
        var newMotivationRef = motivationsRefs.stream().filter(m->m.getUuid().equals(newMotivationUUID)).findFirst();
        if(currentMotivation.isPresent()) {
            if(!currentMotivation.get().getMotivationRef().getUuid().equals(newMotivationUUID)) {
                currentMotivation.get().end();
                needEntity.addMotivation(new MotivationEntity(needEntity,newMotivationRef.orElseThrow(() -> new RuntimeException("Unknown Motivation Reference Type"))));
            }
        } else {
            needEntity.addMotivation(new MotivationEntity(needEntity, newMotivationRef.orElseThrow(() -> new RuntimeException("Unknown Motivation Reference Type"))));
        }
        return needEntity;
    }


    private void addMotivation(MotivationEntity motivationEntity) {
        motivations.add(motivationEntity);
    }

}
