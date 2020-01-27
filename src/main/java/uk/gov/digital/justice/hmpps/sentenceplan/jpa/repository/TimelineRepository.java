package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.TimelineEntity;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface TimelineRepository extends CrudRepository<TimelineEntity, Long> {


    Stream<TimelineEntity> findBySentencePlanUUID(UUID sentencePlanUUID);

}
