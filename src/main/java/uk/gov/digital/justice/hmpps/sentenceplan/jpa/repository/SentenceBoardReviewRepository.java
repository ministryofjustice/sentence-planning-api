package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentenceBoardReviewEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface SentenceBoardReviewRepository extends CrudRepository<SentenceBoardReviewEntity, Long> {

    @Query(value = "select * from sentence_board_review where sentence_plan_uuid = ?1", nativeQuery = true)
    List<SentenceBoardReviewEntity> findAllBySentencePlanUUID(UUID sentencePlanUUID);

    SentenceBoardReviewEntity findByUuid(UUID sentenceBoardReviewUUID);
}
