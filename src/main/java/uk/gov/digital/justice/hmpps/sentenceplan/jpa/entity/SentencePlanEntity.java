package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@AllArgsConstructor
@NoArgsConstructor
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

    @Builder.Default
    @Column(name = "UUID")
    private UUID uuid = UUID.randomUUID();

    @Builder.Default
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "STARTED_DATE")
    private LocalDateTime startedDate;

    @Column(name = "COMPLETED_DATE")
    private LocalDateTime completedDate;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "DATA")
    private SentencePlanPropertiesEntity data;

    @Column(name = "ASSESSMENT_NEEDS_LAST_IMPORTED_ON")
    private LocalDateTime assessmentNeedsLastImportedOn;

    @ManyToOne
    @JoinColumn(name = "OFFENDER_UUID", referencedColumnName = "UUID")
    private OffenderEntity offender;

    @Builder.Default
    @OneToMany(mappedBy = "sentencePlan", cascade = CascadeType.ALL)
    private List<NeedEntity> needs = new ArrayList<>(0);

    public SentencePlanEntity(OffenderEntity offender) {
        this.offender = offender;
        this.data = new SentencePlanPropertiesEntity();
    }

    public void start() {
       this.startedDate = LocalDateTime.now();
    }

    public void end() {
        this.completedDate = LocalDateTime.now();
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

    public void addObjective(ObjectiveEntity objective) {
        // Set the priority to lowest
        objective.setPriority(this.getObjectives().size());
         this.data.getObjectives().put(objective.getId(), objective);
    }

    public ObjectiveEntity getObjective(UUID objectiveUUID) {
       return this.data.getObjectives().get(objectiveUUID);
    }

    public Map<UUID, ObjectiveEntity> getObjectives() {
        return this.data.getObjectives();
    }

    public void addComment(CommentEntity commentEntity) {
        this.data.addComment(commentEntity);
    }

    private void addNeed(NeedEntity need) {
        need.setActive(true);
        this.needs.add(need);
    }
}


