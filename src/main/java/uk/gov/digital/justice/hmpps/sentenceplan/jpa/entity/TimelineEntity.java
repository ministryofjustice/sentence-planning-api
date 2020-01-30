package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
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

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "PAYLOAD")
    private String payload;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "EVENT_TIMESTAMP")
    private LocalDateTime eventTimestamp;

    @Column(name = "TYPE")
    private LogEvent type;

    public TimelineEntity(UUID sentencePlanUUID, LogEvent type, String user, String to) {
        this.uuid = UUID.randomUUID();
        this.sentencePlanUUID = sentencePlanUUID;
        this.payload = to;
        this.eventTimestamp = LocalDateTime.now();
        this.type = type;
        this.userId = user;
    }

}