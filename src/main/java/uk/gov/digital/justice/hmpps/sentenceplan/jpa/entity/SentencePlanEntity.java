package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
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
    private UUID uuid = UUID.randomUUID();;

    @Column(name = "COMPLETED_DATE")
    private LocalDateTime completedDate;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "DATA")
    private SentencePlanPropertiesEntity data;

    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "ASSESSMENT_NEEDS_LAST_IMPORTED_ON")
    private LocalDateTime assessmentNeedsLastImportedOn;

    @ManyToOne
    @JoinColumn(name = "OFFENDER_UUID", referencedColumnName = "UUID")
    private OffenderEntity offender;

    @OneToMany(mappedBy = "sentencePlan", cascade = CascadeType.PERSIST)
    private List<NeedEntity> needs = new ArrayList<>(0);

    public SentencePlanEntity(OffenderEntity offender) {
        this.offender = offender;
        this.data = new SentencePlanPropertiesEntity();
    }

    public void updateNeeds(List<NeedEntity> needs) {
        var latestNeeds = needs.stream().map(NeedEntity::getDescription).collect(Collectors.toSet());
        var currentNeeds = this.needs.stream().map(NeedEntity::getDescription).collect(Collectors.toSet());

        //flag removed needs as inactive
        this.needs.stream().filter(n-> !latestNeeds.contains(n.getDescription())).forEach(
                n->n.setActive(false)
        );

        //add new needs
        needs.stream().filter(n-> !currentNeeds.contains(n.getDescription())).forEach(
                this::addNeed
        );
    }

    public void setSafeguardingRisks(Boolean childSafeguardingIndicated, Boolean complyWithChildProtectionPlanIndicated) {
        this.data.setChildSafeguardingIndicated(childSafeguardingIndicated);
        this.data.setComplyWithChildProtectionPlanIndicated(complyWithChildProtectionPlanIndicated);
    }

    public ObjectiveEntity getObjective(UUID objectiveUUID) {
       return this.data.getObjectives().get(objectiveUUID);
    }

    public void addComment(CommentEntity commentEntity) {
        this.data.addComment(commentEntity);
    }

    private void addNeed(NeedEntity need) {
        this.needs.add(need);
    }
}


