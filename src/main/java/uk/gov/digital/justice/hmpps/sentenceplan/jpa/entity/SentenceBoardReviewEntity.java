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
@Table(name = "SENTENCE_BOARD_REVIEW")
public class SentenceBoardReviewEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "UUID")
    private UUID uuid;

    @Column(name = "COMMENTS")
    private String comments;

    @Column(name = "ATTENDEES")
    private String attendees;

    @Column(name = "DATE_OF_BOARD")
    private LocalDateTime dateOfBoard;

    @ManyToOne
    @JoinColumn(name = "SENTENCE_PLAN_UUID", referencedColumnName = "UUID")
    private SentencePlanEntity sentencePlan;

    public SentenceBoardReviewEntity(String comments, String attendees, SentencePlanEntity sentencePlan) {
        this.uuid = UUID.randomUUID();
        this.comments = comments;
        this.attendees = attendees;
        this.sentencePlan = sentencePlan;
    }

}
