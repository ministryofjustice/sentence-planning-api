package uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository;

        import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
        import org.springframework.data.repository.CrudRepository;
        import org.springframework.stereotype.Repository;

        import java.util.UUID;

@Repository
public interface SentencePlanRepository extends CrudRepository<SentencePlanEntity, Long> {

    SentencePlanEntity findByUuid(UUID uuid);

    SentencePlanEntity findByOffenderUuid(UUID offenderUUID);
}
