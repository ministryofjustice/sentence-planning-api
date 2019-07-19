package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "OFFENDER")
public class OffenderEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "UUID")
    private UUID uuid;

    @Column(name = "OASYS_OFFENDER_ID")
    private String oasysOffednerId;

    @Column(name = "NOMIS_OFFENDER_ID")
    private String nomisOffednerId;

    @Column(name = "DELIUS_OFFENDER_ID")
    private String deliusOffednerId;

    @OneToMany(mappedBy = "offender", cascade = CascadeType.PERSIST)
    private List<SentencePlanEntity> setencePlans;

    public OffenderEntity(String oasysOffednerId, String nomisOffednerId) {
        this.uuid = UUID.randomUUID();
        this.oasysOffednerId = oasysOffednerId;
        this.nomisOffednerId = nomisOffednerId;
        this.setencePlans = new ArrayList<>();
    }
}
