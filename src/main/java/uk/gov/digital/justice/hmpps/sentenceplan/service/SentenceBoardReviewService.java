package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentenceBoardReviewEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentenceBoardReviewRepository;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent.*;

@Service
@Slf4j
public class SentenceBoardReviewService {

private SentenceBoardReviewRepository sentenceBoardReviewRepository;
private SentencePlanService sentencePlanService;

    public SentenceBoardReviewService(SentenceBoardReviewRepository sentenceBoardReviewRepository, SentencePlanService sentencePlanService) {
        this.sentenceBoardReviewRepository = sentenceBoardReviewRepository;
        this.sentencePlanService = sentencePlanService;
    }

    @Transactional
    public void  addSentenceBoardReview(UUID sentencePlanUUID, String comments, String attendees, LocalDate dateOfBoard) {
        SentencePlanEntity sentencePlan = sentencePlanService.getSentencePlanEntity(sentencePlanUUID);
        SentenceBoardReviewEntity sentenceBoardReviewEntity = new SentenceBoardReviewEntity(comments, attendees, dateOfBoard, sentencePlan);
        sentenceBoardReviewRepository.save(sentenceBoardReviewEntity);
        log.info("Created Sentence Board Review {} for Sentence Plan {}", sentenceBoardReviewEntity.getUuid(), sentencePlanUUID, value(EVENT, SENTENCE_BOARD_REVIEW_CREATED));
    }

    public List<SentenceBoardReviewEntity> getSentenceBoardReviewsBySentencePlanUUID(UUID sentencePlanUUID) {
        var boardReviews = sentenceBoardReviewRepository.findAllBySentencePlanUUID(sentencePlanUUID);
        log.info("Retrieved {} Sentence Board Reviews for Sentence Plan {}", boardReviews.size(), sentencePlanUUID, value(EVENT, SENTENCE_BOARD_REVIEWS_RETRIEVED));
        return boardReviews;
    }

    public SentenceBoardReviewEntity getSentenceBoardReviewBySentencePlanUUID(UUID sentencePlanUUID, UUID sentenceBoardReviewUUID) {
        var boardReview = getSentenceBoardReviewEntity(sentenceBoardReviewUUID);
        log.info("Retrieved {} Sentence Board Review {} for Sentence Plan {}", boardReview.getUuid(), sentencePlanUUID, value(EVENT, SENTENCE_BOARD_REVIEW_RETRIEVED));
        return boardReview;
    }

    private SentenceBoardReviewEntity getSentenceBoardReviewEntity(UUID sentenceBoardReviewUUID) {
        return Optional.ofNullable(sentenceBoardReviewRepository.findByUuid(sentenceBoardReviewUUID))
                .orElseThrow(() -> new EntityNotFoundException(String.format("Sentence Board Review %s not found", sentenceBoardReviewUUID)));
    }

}
