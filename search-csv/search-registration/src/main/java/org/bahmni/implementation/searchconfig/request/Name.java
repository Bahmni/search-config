package org.bahmni.implementation.searchconfig.request;

import lombok.Data;

@Data
public class Name {
    private String familyName;
    private String middleName;
    private String givenName;

    public Name(String givenName, String middleName, String familyName) {
        this.familyName = familyName;
        this.middleName = middleName;
        this.givenName = givenName;
    }
}
