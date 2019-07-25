package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.MotivationEntity;

import java.util.List;

@Repository
public interface MotivationRespository extends CrudRepository<MotivationEntity, Long> {

    List<MotivationEntity> findAllByDeletedIsNull();
}
