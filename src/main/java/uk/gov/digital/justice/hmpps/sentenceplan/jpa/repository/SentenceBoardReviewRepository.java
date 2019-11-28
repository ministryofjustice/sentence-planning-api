package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentenceBoardReviewEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface SentenceBoardReviewRepository extends CrudRepository<SentenceBoardReviewEntity, Long> {

    List<SentenceBoardReviewEntity> findAllBySentencePlan(UUID sentencePlanUUID);
}
