package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.TimelineEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface TimelineRepository extends CrudRepository<TimelineEntity, Long> {

    List<TimelineEntity> findBySentencePlanUUIDAndEntityKey(UUID sentencePlanUUID, String entityKey);

}