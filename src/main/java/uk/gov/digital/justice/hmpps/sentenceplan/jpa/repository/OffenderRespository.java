package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OffenderRespository extends CrudRepository<OffenderEntity, Long> {

    Optional<OffenderEntity> findByOasysOffenderId(long offenderId);

    @Query(value = "select o.* from offender o inner join sentence_plan s on o.uuid = s.offender_uuid where s.uuid = ?1", nativeQuery = true)
    OffenderEntity findOffenderBySentencePlanUuid(UUID sentencePlanUuid);
}
