package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "MOTIVATION")
public class MotivationEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "UUID")
    private UUID uuid;

    @Column(name = "NEED_UUID")
    private UUID needUuid;

    @Column(name = "MOTIVATION_REF_UUID")
    private UUID motivationRefUuid;

    @Column(name = "START_DATE")
    private LocalDateTime start;

    @Column(name = "END_DATE")
    private LocalDateTime end;

    public MotivationEntity(UUID needUuid, UUID motivationRefUuid) {
        this.uuid = UUID.randomUUID();
        this.needUuid = needUuid;
        this.motivationRefUuid = motivationRefUuid;
        this.start = LocalDateTime.now();
    }

    public boolean isEnded() {
        return this.end != null;
    }

    public void end() {
        this.end = LocalDateTime.now();
    }
}
