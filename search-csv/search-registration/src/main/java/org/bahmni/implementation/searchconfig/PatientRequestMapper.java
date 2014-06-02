package org.bahmni.implementation.searchconfig;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.bahmni.implementation.searchconfig.request.IdentifierType;
import org.bahmni.implementation.searchconfig.request.Name;
import org.bahmni.implementation.searchconfig.request.Patient;
import org.bahmni.implementation.searchconfig.request.PatientAddress;
import org.bahmni.implementation.searchconfig.request.PatientIdentifier;
import org.bahmni.implementation.searchconfig.request.PatientProfileRequest;
import org.bahmni.implementation.searchconfig.request.Person;
import org.bahmni.implementation.searchconfig.response.PatientResponse;
import org.bahmni.implementation.searchconfig.response.PersonResponse;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PatientRequestMapper {

    public static PatientProfileRequest mapPatient(SearchCSVRow csvRow, boolean fromOldCaseNumber) throws ParseException {
        Person person = mapPerson(csvRow, null);
        person.setPersonDateCreated(getDateCreated(csvRow, fromOldCaseNumber));
        List<PatientIdentifier> identifiers;
        identifiers = mapPatientIdentifier(csvRow, fromOldCaseNumber);
        Patient patient = new Patient(person, identifiers);
        return new PatientProfileRequest(patient);
    }

    public static PatientProfileRequest mapPatientForUpdate(SearchCSVRow csvRow, PatientResponse patientResponse) {
        Person person = mapPerson(csvRow, patientResponse.getPerson());
        List<PatientIdentifier> identifiers;
        identifiers = getIdentifiers(csvRow.oldCaseNo);
        Patient patient = new Patient(person, identifiers);
        return new PatientProfileRequest(patient);
    }

    private static Person mapPerson(SearchCSVRow csvRow, PersonResponse personResponse) {
        Person person = mapName(csvRow, personResponse);
        if (personResponse != null) {
            person.setUuid(personResponse.getUuid());
        }
        mapAddress(person, personResponse);
        person.setBirthdate("2011-05-01");
        person.setBirthdateEstimated(true);
        person.setGender("F");
        return person;
    }

    private static Date getDateCreated(SearchCSVRow csvRow, boolean fromOldCaseNumber) throws ParseException {
        if (fromOldCaseNumber) {
            return getDateCreatedFromOldCaseNumber(csvRow);

        } else {
            return getDateCreatedFromVisitDate(csvRow);
        }
    }

    private static Date getDateCreatedFromOldCaseNumber(SearchCSVRow csvRow) {
        int january = 0;
        String oldCaseNumber = csvRow.oldCaseNo;
        String[] caseNumberParts = oldCaseNumber.split("/");
        String yearOfRegistrationString = "20" + caseNumberParts[1];
        Integer yearOfRegistration = Integer.parseInt(yearOfRegistrationString);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, yearOfRegistration);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.MONTH, january);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static Date getDateCreatedFromVisitDate(SearchCSVRow csvRow) throws ParseException {
        Date date = DateUtils.parseDateStrictly(csvRow.visit_date, new String[]{"dd/M/yyyy"});
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static List<PatientIdentifier> mapPatientIdentifier(SearchCSVRow csvRow, boolean fromOldCaseNumber) {
        List<PatientIdentifier> identifiers;
        if (fromOldCaseNumber) {
            identifiers = getIdentifiers(csvRow.oldCaseNo);

        } else {
            identifiers = getIdentifiers(csvRow.newCaseNo);
        }
        return identifiers;
    }


    private static void mapAddress(Person person, PersonResponse personResponse) {
        PatientAddress patientAddress;
        if (personResponse != null && personResponse.getPreferredAddress() != null) {
            String personAddressUuid = personResponse.getPreferredAddress().getUuid();
            patientAddress = new PatientAddress(personAddressUuid, "address1", "address2", "address3", "cityVillage", "state", "country");
        } else {
            patientAddress = new PatientAddress("address1", "address2", "address3", "cityVillage", "state", "country");
        }
        person.setAddresses(Arrays.asList(patientAddress));
    }

    private static List<PatientIdentifier> getIdentifiers(String caseNumber) {
        return Arrays.asList(new PatientIdentifier("SEA" + caseNumber, new IdentifierType("Bahmni Id"), true));
    }

    private static Person mapName(SearchCSVRow csvRow, PersonResponse personResponse) {
        Person person = new Person();
        Name patientName;
        String firstName = csvRow.firstName;
        String middleName = csvRow.middleName;
        String lastName = csvRow.lastName;
        if (StringUtils.isEmpty(lastName) && StringUtils.isNotEmpty(middleName)) {
            lastName = middleName;
            middleName = "";
        }
        if (personResponse != null && personResponse.getPreferredName() != null) {
            String personNameUuid = personResponse.getPreferredName().getUuid();
            patientName = new Name(personNameUuid, firstName, middleName, lastName);
        } else {
            patientName = new Name(firstName, middleName, lastName);
        }
        person.addName(patientName);
        return person;
    }
}
