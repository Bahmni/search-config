package org.bahmni.implementation.searchconfig.request;

import lombok.Data;

@Data
public class Name {
    private String uuid;
    private String prefix;
    private String familyName;
    private String middleName;
    private String givenName;

    public Name(String prefix, String givenName, String middleName, String familyName) {
        this.prefix = prefix;
        this.familyName = familyName;
        this.middleName = middleName;
        this.givenName = givenName;
    }

    public Name(String uuid, String prefix, String givenName, String middleName, String familyName) {
        this.uuid = uuid;
        this.prefix = prefix;
        this.familyName = familyName;
        this.middleName = middleName;
        this.givenName = givenName;
    }
}
