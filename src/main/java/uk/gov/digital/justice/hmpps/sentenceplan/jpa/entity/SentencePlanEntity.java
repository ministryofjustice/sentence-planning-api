package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "SENTENCE_PLAN")
public class SentencePlanEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "UUID")
    @Getter
    private UUID uuid;

    @Column(name = "CREATED_ON")
    @Getter
    private LocalDateTime created;

}
