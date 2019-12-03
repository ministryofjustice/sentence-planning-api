package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.InterventionRefEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.NeedEntity;

import java.util.List;

@Repository
public interface InterventionRespository extends CrudRepository<InterventionRefEntity, Long> {
    List<InterventionRefEntity> findAllByActiveIsTrue();
    List<InterventionRefEntity> findAll();
}
