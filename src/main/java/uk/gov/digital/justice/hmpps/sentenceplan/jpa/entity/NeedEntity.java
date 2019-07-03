package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
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

    @Column(name = "REOFFENDING_RISK")
    private boolean reoffendingRisk;

    @Column(name = "HARM_RISK")
    private boolean harmRisk;

    @Column(name = "LOW_SCORE_RISK")
    private boolean lowScoreRisk;

    @Column(name = "ACTIVE")
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "ASSESSMENT_UUID", referencedColumnName = "UUID")
    private AssessmentEntity assessment;
}
