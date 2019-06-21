package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SentencePlanRespository extends CrudRepository<SentencePlanEntity, Long> {


}
