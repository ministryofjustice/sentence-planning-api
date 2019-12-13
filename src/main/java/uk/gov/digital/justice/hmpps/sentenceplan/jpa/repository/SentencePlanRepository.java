package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SentencePlanRepository extends CrudRepository<SentencePlanEntity, Long> {

    SentencePlanEntity findByUuid(UUID uuid);

    List<SentencePlanEntity> findByOffenderUuid(UUID offenderUUID);
}
