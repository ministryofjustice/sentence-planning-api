package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentenceBoardReviewEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentenceBoardReviewRepository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SentenceBoardReviewService {

private SentenceBoardReviewRepository sentenceBoardReviewRepository;

    public SentenceBoardReviewService(SentenceBoardReviewRepository sentenceBoardReviewRepository) {
        this.sentenceBoardReviewRepository = sentenceBoardReviewRepository;
    }

    public List<SentenceBoardReviewEntity> getSentenceBoardReviewBySentencePlanUUID(UUID sentencePlanUUID) {
        return sentenceBoardReviewRepository.findAllBySentencePlan(sentencePlanUUID);
    }

}
