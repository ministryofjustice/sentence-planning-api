package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Offender {
    @JsonProperty("oasysOffenderId")
    private Long oasysOffenderId;
    @JsonProperty("nomisBookingNumber")
    private Long nomisBookingNumber;

    public static Offender from(OffenderEntity offenderEntity) {
        return new Offender(offenderEntity.getOasysOffenderId(), offenderEntity.getNomisBookingNumber());
    }

}
