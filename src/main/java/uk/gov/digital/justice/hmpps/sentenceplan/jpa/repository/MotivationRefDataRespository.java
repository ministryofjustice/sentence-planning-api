package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.MotivationRefEntity;
import java.util.List;

@Repository
public interface MotivationRefDataRespository extends CrudRepository<MotivationRefEntity, Long> {

    List<MotivationRefEntity> findAllByDeletedIsNull();
}
