package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.AssessmentEntity;

@Repository
public interface NeedsRespository extends CrudRepository<AssessmentEntity, Long> {


}
