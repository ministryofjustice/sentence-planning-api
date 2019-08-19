package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;

import java.util.Optional;

@Repository
public interface OffenderRespository extends CrudRepository<OffenderEntity, Long> {

    Optional<OffenderEntity> findByOasysOffenderId(long offenderId);

}
