package org.bahmni.implementation.searchconfig.response;

import lombok.Data;

import java.util.Date;

@Data
public class PatientResponse {
    String uuid;
    String givenName;
    String middleName;
    String familyName;
    String gender;
    String age;
    String birthDate;
    String deathDate;
    String cityVillage;
    String activeVisitUuid;
    String identifier;
    String localName;
    Date dateCreated;
}
