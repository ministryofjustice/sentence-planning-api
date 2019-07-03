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
@Table(name = "ASSESSMENT")
public class AssessmentEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "UUID")
    private UUID uuid;

    @Column(name = "ASSESSMENT_ID")
    private String assessmentId;

    @ManyToOne
    @JoinColumn(name = "SENTENCE_PLAN_UUID", referencedColumnName = "UUID")
    private SentencePlanEntity sentencePlan;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<NeedEntity> needs;

}
