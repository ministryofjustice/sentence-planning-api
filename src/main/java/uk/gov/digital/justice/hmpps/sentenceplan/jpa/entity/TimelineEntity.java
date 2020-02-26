package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Table(name = "TIMELINE")
public class TimelineEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "UUID")
    private UUID uuid;

    @Column(name = "SENTENCE_PLAN_UUID")
    private UUID sentencePlanUUID;

    @Column(name = "ENTITY_KEY")
    private String entityKey;

    @Getter
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "COMMENT")
    private String comment;

    @Getter
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "OBJECTIVE")
    private String objective;

    @Getter
    @Column(name = "user_id")
    private String userId;

    @Getter
    @Column(name = "EVENT_TIMESTAMP")
    private LocalDateTime eventTimestamp;

    @Getter
    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private LogEvent type;

    public TimelineEntity(UUID sentencePlanUUID, LogEvent type, String entityKey, String user, String comment, String objective) {
        this.uuid = UUID.randomUUID();
        this.sentencePlanUUID = sentencePlanUUID;
        this.comment = comment;
        this.objective = objective;
        this.eventTimestamp = LocalDateTime.now();
        this.type = type;
        this.entityKey = entityKey;
        this.userId = user;
    }

}