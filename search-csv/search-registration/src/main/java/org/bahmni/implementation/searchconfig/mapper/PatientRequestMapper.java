package org.bahmni.implementation.searchconfig.mapper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
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

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class PatientRequestMapper {
    private static Logger logger = Logger.getLogger(PatientRequestMapper.class.getName());

    protected static Properties TAHSIL_TO_DISTRICT = new Properties();
    protected static Properties DISTRICT_TO_STATE = new Properties();
    protected static Properties TAHSIL_KRISHNA_TO_ENGLISH = new Properties();

    public PatientRequestMapper() {
        initializeProperties();
    }

    private void initializeProperties() {
        try {
            TAHSIL_TO_DISTRICT.load(getClass().getClassLoader().getResourceAsStream("tahsilDistrictMapping.properties"));
            DISTRICT_TO_STATE.load(getClass().getClassLoader().getResourceAsStream("districtStateMapping.properties"));
            TAHSIL_KRISHNA_TO_ENGLISH.load(getClass().getClassLoader().getResourceAsStream("tehsilKrishnaToEnglish.properties"));
        } catch (IOException e) {
            logger.error("Could not load tahsil to district mapping file.", e);
        }
    }

    public PatientProfileRequest mapPatient(SearchCSVRow csvRow, String caseNumber, AllPatientAttributeTypes allPatientAttributeTypes, boolean shouldRunTransform) throws ParseException {
        Person person = new Person();
        person.setPersonDateCreated(getDateCreated(csvRow, caseNumber));
        mapPerson(csvRow, null, allPatientAttributeTypes, person, shouldRunTransform);
        List<PatientIdentifier> identifiers;
        identifiers = mapPatientIdentifier(csvRow, caseNumber);
        Patient patient = new Patient(person, identifiers);
        return new PatientProfileRequest(patient);
    }

    public PatientProfileRequest mapPatientForUpdate(SearchCSVRow csvRow, PatientResponse patientResponse, AllPatientAttributeTypes allPatientAttributeTypes, Boolean shouldRunTransform) {
        Person person = new Person();
        mapPerson(csvRow, patientResponse.getPerson(), allPatientAttributeTypes, person, shouldRunTransform);
        List<PatientIdentifier> identifiers;
        identifiers = getIdentifiers(csvRow.oldCaseNo);
        Patient patient = new Patient(person, identifiers);
        return new PatientProfileRequest(patient);
    }

    private static Person mapPerson(SearchCSVRow csvRow, PersonResponse personResponse, AllPatientAttributeTypes allPatientAttributeTypes, Person person, boolean shouldRunTransform) {
        mapName(csvRow, personResponse, person);
        if (personResponse != null) {
            person.setUuid(personResponse.getUuid());
        }
        mapAddress(csvRow, person, personResponse, shouldRunTransform);
        mapBirthDate(csvRow, person);
        mapGender(csvRow, person);
        mapAttributes(csvRow, person, allPatientAttributeTypes, shouldRunTransform);
        return person;
    }

    private static void mapAttributes(SearchCSVRow csvRow, Person person, AllPatientAttributeTypes allPatientAttributeTypes, boolean shouldRunTransform) {
        if (allPatientAttributeTypes == null) {
            logger.error("No patient attributes found");
            return;
        }
        if(shouldRunTransform){
            Name preferredName = person.getNames().get(0);
            String givenNameLocal = preferredName.getGivenName();
            String middleNameLocal = preferredName.getMiddleName();
            String familyNameLocal = preferredName.getFamilyName();
            addPersonAttribute(person, allPatientAttributeTypes, givenNameLocal, "givenNameLocal");
            addPersonAttribute(person, allPatientAttributeTypes, middleNameLocal, "middleNameLocal");
            addPersonAttribute(person, allPatientAttributeTypes, familyNameLocal, "familyNameLocal");
        }
        String mobileNumber = csvRow.mobileNumber;
        addPersonAttribute(person, allPatientAttributeTypes, mobileNumber, "Mobile");
    }

    private static void addPersonAttribute(Person person, AllPatientAttributeTypes allPatientAttributeTypes,
                                           String attributeValue, String attributeTypeName) {
        String attributeUUID = allPatientAttributeTypes.getAttributeUUID(attributeTypeName);
        if (StringUtils.isEmpty(attributeUUID)) {
            logger.error("Patient attribute type not found for: " + attributeTypeName);
        } else {
            if(StringUtils.isEmpty(attributeValue))
                return;
            person.addAttribute(new PatientAttribute(attributeUUID, attributeValue));
        }
    }

    private static void mapGender(SearchCSVRow csvRow, Person person) {
        String sanitizedGender = csvRow.gender.trim().toUpperCase();
        if (sanitizedGender.equalsIgnoreCase("M")) {
            person.setGender("M");
        } else if (sanitizedGender.equalsIgnoreCase("F")) {
            person.setGender("F");
        } else if (sanitizedGender.equalsIgnoreCase("O")) {
            person.setGender("O");
        } else {
            person.setGender(".");
        }
    }

    private static void mapBirthDate(SearchCSVRow csvRow, Person person) {
        if (StringUtils.isEmpty(csvRow.age))
            return;
        try {
            String[] split = csvRow.age.split("(?<=[ymd])\\s*");
            Integer years = 0, months = 0, days = 0;
            for (String s : split) {
                if (s.endsWith("y")) {
                    years = Integer.parseInt(s.replace('y', ' ').trim());
                } else if (s.endsWith("m")) {
                    months = Integer.parseInt(s.replace('m', ' ').trim());
                }
                if (s.endsWith("d")) {
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
        } catch (Exception e) {
            logger.debug("Not setting birthDate for : " + csvRow.newCaseNo + "|" + csvRow.oldCaseNo);
        }
    }

    private static Date getDateCreated(SearchCSVRow csvRow, String caseNumber) throws ParseException {
        if (caseNumber != null) {
            return DateMapper.getVisitDateFromCaseNumber(caseNumber);

        } else {
            return DateMapper.getDateFromVisitDate(csvRow);
        }
    }

    private static List<PatientIdentifier> mapPatientIdentifier(SearchCSVRow csvRow, String caseNumber) {
        List<PatientIdentifier> identifiers;
        if (caseNumber != null) {
            identifiers = getIdentifiers(caseNumber);

        } else {
            identifiers = getIdentifiers(csvRow.newCaseNo);
        }
        return identifiers;
    }


    private static void mapAddress(SearchCSVRow csvRow, Person person, PersonResponse personResponse, boolean shouldRunTransform) {
        String cityVillage = csvRow.village;
        String tehsil = org.apache.commons.lang3.StringUtils.isNotEmpty(csvRow.tehsil) ? csvRow.tehsil.trim() : "";
        String countyDistrict = "";
        String stateProvince = "";
        if (shouldRunTransform && TAHSIL_KRISHNA_TO_ENGLISH != null){
            tehsil = TAHSIL_KRISHNA_TO_ENGLISH.getProperty(tehsil);
            if(tehsil == null){
                tehsil = "";
            }
        }
        if (TAHSIL_TO_DISTRICT != null) {
            countyDistrict = TAHSIL_TO_DISTRICT.getProperty(WordUtils.capitalizeFully(tehsil));
            if(countyDistrict == null){
                countyDistrict = "";
            }
        }
        if (DISTRICT_TO_STATE != null) {
            stateProvince = DISTRICT_TO_STATE.getProperty(WordUtils.capitalizeFully(countyDistrict));
            if(stateProvince == null){
                stateProvince = "";
            }
        }
        String country = "";
        PatientAddress patientAddress;
        if (personResponse != null && personResponse.getPreferredAddress() != null) {
            String personAddressUuid = personResponse.getPreferredAddress().getUuid();
            patientAddress = new PatientAddress(personAddressUuid, tehsil, cityVillage, countyDistrict, stateProvince, country);
        } else {
            patientAddress = new PatientAddress(tehsil, cityVillage, countyDistrict, stateProvince, country);
        }
        if (StringUtils.isEmpty(tehsil) || StringUtils.isEmpty(cityVillage)
                || StringUtils.isEmpty(countyDistrict) || StringUtils.isEmpty(cityVillage)) {
            logger.info("Some of the address fields are empty for: " + csvRow.newCaseNo + "|" + csvRow.oldCaseNo);
        }
        person.setAddresses(Arrays.asList(patientAddress));
    }

    private static List<PatientIdentifier> getIdentifiers(String caseNumber) {
        return Arrays.asList(new PatientIdentifier("SEA" + caseNumber, new IdentifierType("Bahmni Id"), true));
    }

    private static Person mapName(SearchCSVRow csvRow, PersonResponse personResponse, Person person) {
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
