package org.bahmni.implementation.searchconfig.request;

import lombok.Data;

@Data
public class Name {
    private String uuid;
    private String familyName;
    private String middleName;
    private String givenName;

    public Name(String givenName, String middleName, String familyName) {
        this.familyName = familyName;
        this.middleName = middleName;
        this.givenName = givenName;
    }

    public Name(String uuid, String givenName, String middleName, String familyName) {
        this.uuid = uuid;
        this.familyName = familyName;
        this.middleName = middleName;
        this.givenName = givenName;
    }
}
