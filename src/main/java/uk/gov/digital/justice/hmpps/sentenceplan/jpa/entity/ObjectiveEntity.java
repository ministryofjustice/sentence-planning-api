package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ObjectiveEntity implements Serializable {

    private UUID id;

    private String description;

    private List<UUID> needs = new ArrayList<>(0);

    private Map<UUID, ActionEntity> actions = new HashMap<>(0);

    private LocalDateTime created = LocalDateTime.now();

    public ObjectiveEntity(String description) {
        this.id = UUID.randomUUID();
        this.description = description;
    }

    public void addAction(ActionEntity actionEntity) {
        // Set the priority to lowest
        actionEntity.setPriority(this.getActions().size());
        actions.put(actionEntity.getId(), actionEntity);
    }

    public ActionEntity getAction(UUID actionUUID) {
        return actions.get(actionUUID);
    }

}
