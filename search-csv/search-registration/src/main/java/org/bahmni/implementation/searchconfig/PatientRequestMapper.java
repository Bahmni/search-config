package org.bahmni.implementation.searchconfig;

import org.apache.commons.lang.StringUtils;
import org.bahmni.implementation.searchconfig.request.IdentifierType;
import org.bahmni.implementation.searchconfig.request.Name;
import org.bahmni.implementation.searchconfig.request.Patient;
import org.bahmni.implementation.searchconfig.request.PatientAddress;
import org.bahmni.implementation.searchconfig.request.PatientIdentifier;
import org.bahmni.implementation.searchconfig.request.PatientProfileRequest;
import org.bahmni.implementation.searchconfig.request.Person;

import java.util.Arrays;
import java.util.List;

public class PatientRequestMapper {
    public static PatientProfileRequest mapPatient(SearchCSVRow csvRow, boolean fromOldCaseNumber) {
        Person person = mapPerson(csvRow);
        List<PatientIdentifier> identifiers;
        if(fromOldCaseNumber){
            identifiers = getIdentifiers(csvRow.oldCaseNo);
        } else{
            identifiers = getIdentifiers(csvRow.newCaseNo);
        }
        Patient patient = new Patient(person, identifiers);
        return new PatientProfileRequest(patient);
    }

    private static Person mapPerson(SearchCSVRow csvRow) {
        Person person = new Person();
        mapName(csvRow, person);
        person.setAddresses(Arrays.asList(new PatientAddress("address1", "address2", "address3", "cityVillage", "state", "country")));
        person.setBirthdate("2011-05-01");
        person.setBirthdateEstimated(true);
        person.setGender("F");
        return person;
    }

    private static List<PatientIdentifier> getIdentifiers(String caseNumber) {
        return Arrays.asList(new PatientIdentifier("SEA" + caseNumber, new IdentifierType("Bahmni Id"), true));
    }

    private static void mapName(SearchCSVRow csvRow, Person person) {
        String firstName = csvRow.firstName;
        String middleName = csvRow.middleName;
        String lastName = csvRow.lastName;
        if (StringUtils.isEmpty(lastName) && StringUtils.isNotEmpty(middleName)) {
            lastName = middleName;
            middleName = "";
        }
        Name patientName = new Name(firstName, middleName, lastName);
        person.addName(patientName);
    }
}
