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
@Table(name = "MOTIVATION_REF_DATA")
public class MotivationRefEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "UUID")
    private UUID uuid;

    @Column(name = "MOTIVATION_TEXT")
    private String motivationText;

    @Column(name = "CREATED")
    private LocalDateTime created;

    @Column(name = "DELETED")
    private LocalDateTime deleted;

    public MotivationRefEntity(String motivationText) {
        this.uuid = UUID.randomUUID();
        this.motivationText = motivationText;
        this.created = LocalDateTime.now();
    }
}
