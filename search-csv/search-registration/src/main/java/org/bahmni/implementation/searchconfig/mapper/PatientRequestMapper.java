package org.bahmni.implementation.searchconfig.mapper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.bahmni.implementation.searchconfig.SearchCSVRow;
import org.bahmni.implementation.searchconfig.request.IdentifierType;
import org.bahmni.implementation.searchconfig.request.Name;
import org.bahmni.implementation.searchconfig.request.Patient;
import org.bahmni.implementation.searchconfig.request.PatientAddress;
import org.bahmni.implementation.searchconfig.request.PatientAttribute;
import org.bahmni.implementation.searchconfig.request.PatientIdentifier;
import org.bahmni.implementation.searchconfig.request.PatientProfileRequest;
import org.bahmni.implementation.searchconfig.request.Person;
import org.bahmni.implementation.searchconfig.response.PatientResponse;
import org.bahmni.implementation.searchconfig.response.PersonResponse;
import org.bahmni.openmrsconnector.AllPatientAttributeTypes;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PatientRequestMapper {
    private static Logger logger= Logger.getLogger(PatientRequestMapper.class.getName());

    public static PatientProfileRequest mapPatient(SearchCSVRow csvRow, boolean fromOldCaseNumber, AllPatientAttributeTypes allPatientAttributeTypes) throws ParseException {
        Person person = mapPerson(csvRow, null, allPatientAttributeTypes);
        person.setPersonDateCreated(getDateCreated(csvRow, fromOldCaseNumber));
        List<PatientIdentifier> identifiers;
        identifiers = mapPatientIdentifier(csvRow, fromOldCaseNumber);
        Patient patient = new Patient(person, identifiers);
        return new PatientProfileRequest(patient);
    }

    public static PatientProfileRequest mapPatientForUpdate(SearchCSVRow csvRow, PatientResponse patientResponse, AllPatientAttributeTypes allPatientAttributeTypes) {
        Person person = mapPerson(csvRow, patientResponse.getPerson(), allPatientAttributeTypes);
        List<PatientIdentifier> identifiers;
        identifiers = getIdentifiers(csvRow.oldCaseNo);
        Patient patient = new Patient(person, identifiers);
        return new PatientProfileRequest(patient);
    }

    private static Person mapPerson(SearchCSVRow csvRow, PersonResponse personResponse, AllPatientAttributeTypes allPatientAttributeTypes) {
        Person person = mapName(csvRow, personResponse);
        if (personResponse != null) {
            person.setUuid(personResponse.getUuid());
        }
        mapAddress(csvRow, person, personResponse);
        mapBirthDate(csvRow, person);
        mapGender(csvRow, person);
        mapAttributes(csvRow, person, allPatientAttributeTypes);
        return person;
    }

    private static void mapAttributes(SearchCSVRow csvRow, Person person, AllPatientAttributeTypes allPatientAttributeTypes) {
        if(allPatientAttributeTypes == null){
            logger.error("No patient attributes found");
            return;
        }
        String givenNameLocal = csvRow.firstName;
        String middleNameLocal = csvRow.middleName;
        String familyNameLocal = csvRow.lastName;
        String mobileNumber = csvRow.mobileNumber;

        person.addAttribute(new PatientAttribute(allPatientAttributeTypes.getAttributeUUID("givenNameLocal"), givenNameLocal));
        person.addAttribute(new PatientAttribute(allPatientAttributeTypes.getAttributeUUID("middleNameLocal"), middleNameLocal));
        person.addAttribute(new PatientAttribute(allPatientAttributeTypes.getAttributeUUID("familyNameLocal"), familyNameLocal));
        person.addAttribute(new PatientAttribute(allPatientAttributeTypes.getAttributeUUID("Mobile"), mobileNumber));
    }

    private static void mapGender(SearchCSVRow csvRow, Person person) {
        if(csvRow.gender.equalsIgnoreCase("M")){
            person.setGender("M");
        }else if(csvRow.gender.equalsIgnoreCase("F")){
            person.setGender("F");
        }if(csvRow.gender.equalsIgnoreCase("O")){
            person.setGender("O");
        }
    }

    private static void mapBirthDate(SearchCSVRow csvRow, Person person) {
        if(StringUtils.isEmpty(csvRow.age))
            return;
        String[] split = csvRow.age.split("(?<=[ymd])\\s*");
        Integer years = 0, months = 0, days = 0;
        for (String s : split) {
            if(s.endsWith("y")){
                years = Integer.parseInt(s.replace('y', ' ').trim());
            }else if(s.endsWith("m")){
                months = Integer.parseInt(s.replace('m', ' ').trim());
            }if(s.endsWith("d")){
                days = Integer.parseInt(s.replace('d', ' ').trim());
            }
        }
        Date dateOfBirth = DateMapper.getDateFromVisitDate(csvRow);
        dateOfBirth = DateUtils.addDays(dateOfBirth, -days);
        dateOfBirth = DateUtils.addMonths(dateOfBirth, -months);
        dateOfBirth = DateUtils.addYears(dateOfBirth, -years);

        String birthDateString = org.bahmni.implementation.searchconfig.DateUtils.stringify(dateOfBirth);
        person.setBirthdate(org.bahmni.implementation.searchconfig.DateUtils.truncateTimeComponent(birthDateString));
        person.setBirthdateEstimated(true);
    }

    private static Date getDateCreated(SearchCSVRow csvRow, boolean fromOldCaseNumber) throws ParseException {
        if (fromOldCaseNumber) {
            return DateMapper.getDateFromOldCaseNumber(csvRow);

        } else {
            return DateMapper.getDateFromVisitDate(csvRow);
        }
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


    private static void mapAddress(SearchCSVRow csvRow, Person person, PersonResponse personResponse) {
        String address3 = csvRow.tehsil;
        String cityVillage = csvRow.village;
        String countyDistrict = csvRow.district;
        String country = "";
        String stateProvince = "";
        PatientAddress patientAddress;
        if (personResponse != null && personResponse.getPreferredAddress() != null) {
            String personAddressUuid = personResponse.getPreferredAddress().getUuid();
            patientAddress = new PatientAddress(personAddressUuid, address3, cityVillage, countyDistrict, stateProvince, country);
        } else {
            patientAddress = new PatientAddress(address3, cityVillage, countyDistrict, stateProvince, country);
        }
        person.setAddresses(Arrays.asList(patientAddress));
    }

    private static List<PatientIdentifier> getIdentifiers(String caseNumber) {
        return Arrays.asList(new PatientIdentifier("SEA" + caseNumber, new IdentifierType("Bahmni Id"), true));
    }

    private static Person mapName(SearchCSVRow csvRow, PersonResponse personResponse) {
        Person person = new Person();
        Name patientName;
        String prefix = csvRow.prefix;
        String firstName = csvRow.firstName;
        String middleName = csvRow.middleName;
        String lastName = csvRow.lastName;
        if (StringUtils.isEmpty(lastName) && StringUtils.isNotEmpty(middleName)) {
            lastName = middleName;
            middleName = "";
        }
        if (personResponse != null && personResponse.getPreferredName() != null) {
            String personNameUuid = personResponse.getPreferredName().getUuid();
            patientName = new Name(personNameUuid, prefix, firstName, middleName, lastName);
        } else {
            patientName = new Name(prefix, firstName, middleName, lastName);
        }
        person.addName(patientName);
        return person;
    }
}
