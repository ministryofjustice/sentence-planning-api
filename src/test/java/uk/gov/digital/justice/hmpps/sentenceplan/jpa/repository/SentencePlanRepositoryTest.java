package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.CommentType;
import uk.gov.digital.justice.hmpps.sentenceplan.application.AuditorAwareImpl;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.CommentEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:sentencePlan/before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:sentencePlan/after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class SentencePlanRepositoryTest {

    @Autowired
    SentencePlanRepository repository;

    @Autowired
    OffenderRespository offenderRepository;

    @MockBean
    AuditorAwareImpl auditorAware;

    @Before
    public void setup() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.ofNullable("USER"));
    }

    @Test
    public void shouldSetCreatedByAndOnWhenSentencePlanCreated() {

        var offender =offenderRepository.findByOasysOffenderId(123456L);
        var sentencePlan = new SentencePlanEntity(offender.get());

        repository.save(sentencePlan);

        var result = repository.findByUuid(sentencePlan.getUuid());

        assertThat(result.getCreatedOn()).isEqualToIgnoringSeconds(LocalDateTime.now());
        assertThat(result.getCreateUserId()).isEqualToIgnoringCase("USER");
    }

    @Test
    public void shouldSetModifiedByAndOnWhenSentencePlanUpdated() {
        var sentencePlan = repository.findByUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        sentencePlan.addComment(new CommentEntity("test", CommentType.YOUR_RESPONSIVITY, null));
        repository.save(sentencePlan);

        var result = repository.findByUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(result.getModifyDateTime()).isEqualToIgnoringSeconds(LocalDateTime.now());
        assertThat(result.getModifyUserId()).isEqualToIgnoringCase("USER");
    }


        @Test
        public void initialRevision() {
            var offender =offenderRepository.findByOasysOffenderId(123456L);
            var sentencePlan = new SentencePlanEntity(offender.get());

            repository.save(sentencePlan);

            var revisions = repository.findRevisions(sentencePlan.getId());

            assertThat(revisions)
                    .isNotEmpty()
                    .allSatisfy(revision -> assertThat(revision.getEntity())
                            .extracting(SentencePlanEntity::getId, SentencePlanEntity::getUuid, SentencePlanEntity::getCreateUserId, SentencePlanEntity::getCreatedOn, SentencePlanEntity::getModifyUserId, SentencePlanEntity::getModifyDateTime)
                            .containsExactly(sentencePlan.getId(), sentencePlan.getUuid(), sentencePlan.getCreateUserId(), sentencePlan.getCreatedOn(), sentencePlan.getModifyUserId(), sentencePlan.getModifyDateTime())
                    );
        }

    @Test
    public void updateIncreasesRevisionNumber() {
        var offender =offenderRepository.findByOasysOffenderId(123456L);
        var sentencePlan = new SentencePlanEntity(offender.get());
        repository.save(sentencePlan);

        sentencePlan.addComment(new CommentEntity("a comment", CommentType.LIAISON_ARRANGEMENTS, "a user"));
        repository.save(sentencePlan);

        assertThat(repository.findRevisions(sentencePlan.getId())).hasSize(2);
        var revision = repository.findLastChangeRevision(sentencePlan.getId());

        assertThat(revision)
                .isPresent()
                .hasValueSatisfying(rev ->
                        assertThat(rev.getRevisionNumber()).hasValue(2)
                )
                .hasValueSatisfying(rev ->
                        assertThat(rev.getEntity().getData().getComments()).hasSize(1));
    }

} 