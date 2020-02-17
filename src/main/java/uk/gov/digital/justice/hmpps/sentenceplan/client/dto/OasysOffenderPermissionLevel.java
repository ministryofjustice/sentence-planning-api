package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import lombok.Getter;

@Getter
public enum OasysOffenderPermissionLevel {
    UNAUTHORISED(0),
    READ_ONLY(1),
    WRITE(2);

    private int accessLevel;

    OasysOffenderPermissionLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }
}
