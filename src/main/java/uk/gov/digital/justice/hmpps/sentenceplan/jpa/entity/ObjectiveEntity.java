package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ObjectiveStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import static uk.gov.digital.justice.hmpps.sentenceplan.api.ObjectiveStatus.CLOSED;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ObjectiveStatus.OPEN;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ObjectiveEntity implements Serializable {

    private UUID id;

    private String description;

    private List<UUID> needs = new ArrayList<>(0);

    private Map<UUID, ActionEntity> actions = new HashMap<>(0);

    private boolean meetsChildSafeguarding = false;

    private ObjectiveStatus status;

    @Setter
    private int priority;

    private LocalDateTime created = LocalDateTime.now();

    private List<ObjectiveStatusEntity> statusChanges = new ArrayList<>(0);

    public ObjectiveEntity(String description, List<UUID> needs, boolean meetsChildSafeguarding) {
        this.id = UUID.randomUUID();
        this.status = OPEN;
        update(description, needs, meetsChildSafeguarding);
    }

    public void updateObjective(String description, List<UUID> needs, boolean meetsChildSafeguarding) {
        update(description, needs, meetsChildSafeguarding );
    }

    public void addAction(ActionEntity actionEntity) {
        if(actionEntity.getPriority() < 1) {
            // Set the priority to lowest
            actionEntity.setPriority(this.getActions().size() + 1);
        }
        actions.put(actionEntity.getId(), actionEntity);
    }

    public ActionEntity getAction(UUID actionUUID) {
        return actions.get(actionUUID);
    }

    private void update(String description, List<UUID> needs, boolean meetsChildSafeguarding) {
        this.description = description;
        this.needs = needs;
        this.meetsChildSafeguarding = meetsChildSafeguarding;
    }

    public void open(String openedBy) {
        if(this.status.equals(CLOSED)) {
            this.statusChanges.add(new ObjectiveStatusEntity(OPEN, null, openedBy));
            this.status = OPEN;
        }
    }

    public void close(String comment, String closedBy) {
        this.actions.forEach((key, action) -> action.abandon());
        this.status = CLOSED;
        this.statusChanges.add(new ObjectiveStatusEntity(CLOSED, comment, closedBy));
    }

}
