package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;


import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import uk.gov.digital.justice.hmpps.sentenceplan.api.EventType;
import uk.gov.digital.justice.hmpps.sentenceplan.api.PlanStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Table(name = "SENTENCE_PLAN")
public class SentencePlanEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "UUID")
    private UUID uuid;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private PlanStatus status;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "DATA")
    private SentencePlanPropertiesEntity data;

    @Column(name = "EVENT_TYPE")
    private EventType eventType;

    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;

    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    @Column(name = "END_DATE")
    private LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "OFFENDER_UUID", referencedColumnName = "UUID")
    private OffenderEntity offender;

    @OneToMany(mappedBy = "sentencePlan", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<AssessmentEntity> assessments;

    public SentencePlanEntity(OffenderEntity offender, AssessmentEntity assessment) {
        this.offender = offender;
        this.assessments = List.of(assessment);
        this.uuid = UUID.randomUUID();
        this.createdOn = LocalDateTime.now();
        this.startDate = LocalDateTime.now();
        this.status = PlanStatus.DRAFT;
        this.eventType = EventType.CREATED;
    }
}


