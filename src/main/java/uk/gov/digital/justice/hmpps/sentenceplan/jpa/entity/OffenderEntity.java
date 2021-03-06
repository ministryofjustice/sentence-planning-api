package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
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
    private Long oasysOffenderId;

    @Column(name = "NOMIS_OFFENDER_ID")
    private String nomisOffenderId;

    @Column(name = "DELIUS_OFFENDER_ID")
    private String deliusOffenderId;

    @Column(name = "NOMIS_BOOKING_NUMBER")
    private String nomisBookingNumber;

    @Setter
    @Column(name = "OASYS_OFFENDER_LAST_IMPORTED_ON")
    private LocalDateTime oasysOffenderLastImportedOn;

    @OneToMany(mappedBy = "offender", cascade = CascadeType.PERSIST)
    private List<SentencePlanEntity> setencePlans;

    public OffenderEntity(Long oasysOffenderId, String nomisOffenderId, String nomisBookingNumber, String deliusOffenderId) {
        this.uuid = UUID.randomUUID();
        this.oasysOffenderId = oasysOffenderId;
        this.nomisOffenderId = nomisOffenderId;
        this.nomisBookingNumber = nomisBookingNumber;
        this.deliusOffenderId = deliusOffenderId;
        this.oasysOffenderLastImportedOn = LocalDateTime.now();
        this.setencePlans = new ArrayList<>();
    }

    public void updateIdentityDetails(Long oasysOffenderId, String nomisOffenderId, String nomisBookingNumber, String deliusOffenderId) {
        this.oasysOffenderId = oasysOffenderId;
        this.nomisOffenderId = nomisOffenderId;
        this.nomisBookingNumber = nomisBookingNumber;
        this.oasysOffenderLastImportedOn = LocalDateTime.now();
        this.deliusOffenderId = deliusOffenderId;
    }
}
