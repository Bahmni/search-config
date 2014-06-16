package org.bahmni.implementation.searchconfig;

import org.bahmni.implementation.searchconfig.mapper.PatientRequestMapper;
import org.bahmni.implementation.searchconfig.request.Name;
import org.bahmni.implementation.searchconfig.request.Patient;
import org.bahmni.implementation.searchconfig.request.PatientAddress;
import org.bahmni.implementation.searchconfig.request.PatientProfileRequest;
import org.bahmni.implementation.searchconfig.request.Person;
import org.bahmni.implementation.searchconfig.response.PatientResponse;
import org.bahmni.implementation.searchconfig.response.PersonResponse;
import org.bahmni.openmrsconnector.AllPatientAttributeTypes;
import org.junit.Test;

import java.text.ParseException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class PatientRequestMapperTest {

    @Test
    public void shouldMapNAmeFromCsvRowWhenAllPartsOfNameExist() throws ParseException {
        SearchCSVRow csvRow = TestUtils.searchCsvBuilder();

        PatientProfileRequest patientProfileRequest = new PatientRequestMapper().mapPatient(csvRow, null, null, false);
        Patient patient = patientProfileRequest.getPatient();

        assertEquals(1, patient.getPerson().getNames().size());
        assertEquals(csvRow.firstName, patient.getPerson().getNames().get(0).getGivenName());
        assertEquals(csvRow.middleName, patient.getPerson().getNames().get(0).getMiddleName());
        assertEquals(csvRow.lastName, patient.getPerson().getNames().get(0).getFamilyName());
    }

    @Test
    public void shouldMapNAme_ByMappingMiddleNameAsLastName_WhenLastNameIsEmpty() throws ParseException {
        SearchCSVRow csvRow = TestUtils.searchCsvBuilder();
        csvRow.lastName = "";

        PatientProfileRequest patientProfileRequest = new PatientRequestMapper().mapPatient(csvRow, null, null, false);
        Patient patient = patientProfileRequest.getPatient();

        assertEquals(1, patient.getPerson().getNames().size());
        assertEquals(csvRow.firstName, patient.getPerson().getNames().get(0).getGivenName());
        assertEquals("", patient.getPerson().getNames().get(0).getMiddleName());
        assertEquals(csvRow.middleName, patient.getPerson().getNames().get(0).getFamilyName());
    }

    @Test
    public void shouldMapOldCaseNumberAsPatientIdentifier() throws ParseException {
        SearchCSVRow csvRow = TestUtils.searchCsvBuilder();
        csvRow.oldCaseNo = "1234/12";
        csvRow.newCaseNo = "";

        PatientProfileRequest patientProfileRequest = new PatientRequestMapper().mapPatient(csvRow, csvRow.oldCaseNo, null, false);
        Patient patient = patientProfileRequest.getPatient();

        assertEquals("SEA" + csvRow.oldCaseNo, patient.getIdentifiers().get(0).getIdentifier());
    }

    @Test
    public void shouldMapNewCaseNumberAsPatientIdentifier() throws ParseException {
        SearchCSVRow csvRow = TestUtils.searchCsvBuilder();

        PatientProfileRequest patientProfileRequest = new PatientRequestMapper().mapPatient(csvRow, null, null, false);
        Patient patient = patientProfileRequest.getPatient();

        assertEquals("SEA" + csvRow.newCaseNo, patient.getIdentifiers().get(0).getIdentifier());
    }

    @Test
    public void shouldMapPersonNameUuidFromPersonResponse() {
        String personNameUuid = "personNameUuid";
        String personUuid = "patientUuid";
        PatientResponse patientResponse = new PatientResponse();
        patientResponse.setUuid(personUuid);
        PersonResponse person = new PersonResponse();
        person.setUuid(personUuid);
        person.setPreferredNameUuid(personNameUuid);
        patientResponse.setPerson(person);
        SearchCSVRow csvRow = TestUtils.searchCsvBuilder();

        PatientProfileRequest patientProfileRequest = new PatientRequestMapper().mapPatientForUpdate(csvRow, patientResponse, null, false);

        Person mappedPerson = patientProfileRequest.getPatient().getPerson();
        Name mappedPersonName = mappedPerson.getNames().get(0);
        assertEquals(personNameUuid, mappedPersonName.getUuid());
        assertEquals(csvRow.firstName, mappedPersonName.getGivenName());
        assertEquals(csvRow.middleName, mappedPersonName.getMiddleName());
        assertEquals(csvRow.lastName, mappedPersonName.getFamilyName());
        assertEquals(personUuid, mappedPerson.getUuid());
    }

    @Test
    public void shouldMapPersonAddressUuidFromPersonResponse() {
        String personAddressUuid = "personAddressUuid";
        String personUuid = "patientUuid";
        PersonResponse person = new PersonResponse();
        person.setUuid(personUuid);
        person.setPreferredAddressUuid(personAddressUuid);
        PatientResponse patientResponse = new PatientResponse();
        patientResponse.setUuid(personUuid);
        patientResponse.setPerson(person);
        SearchCSVRow csvRow = TestUtils.searchCsvBuilder();

        PatientProfileRequest patientProfileRequest = new PatientRequestMapper().mapPatientForUpdate(csvRow, patientResponse, null, false);

        Person mappedPerson = patientProfileRequest.getPatient().getPerson();
        PatientAddress mappedPersonAddress = mappedPerson.getAddresses().get(0);
        assertEquals(personAddressUuid, mappedPersonAddress.getUuid());
        assertEquals(personUuid, mappedPerson.getUuid());
    }

    @Test
    public void shouldMapPersonDateCreatedFromVisitDate() throws ParseException {
        String visit_date = "30/01/2012";
        SearchCSVRow csvRow = TestUtils.searchCsvBuilder();
        csvRow.visit_date = visit_date;
        String expectedCreatedDate = "2012-01-30 10:00:00";

        PatientProfileRequest patientProfileRequest = new PatientRequestMapper().mapPatient(csvRow, null, null, false);

        Person mappedPerson = patientProfileRequest.getPatient().getPerson();
        assertEquals(expectedCreatedDate, mappedPerson.getPersonDateCreated());
    }

    @Test
    public void shouldMapPersonCreatedDateFromOldCaseNumber() throws ParseException {
        String oldCaseNumber = "1234/12";
        String expectedCreatedDate = "2012-01-01 10:00:00";
        SearchCSVRow csvRow = TestUtils.searchCsvBuilder();
        csvRow.oldCaseNo = oldCaseNumber;

        PatientProfileRequest patientProfileRequest = new PatientRequestMapper().mapPatient(csvRow, csvRow.oldCaseNo, null, false);

        assertEquals(expectedCreatedDate, patientProfileRequest.getPatient().getPerson().getPersonDateCreated());
    }

    @Test
    public void shouldMapBirthDate() throws ParseException {
        SearchCSVRow csvRow = TestUtils.searchCsvBuilder();
        csvRow.visit_date = "30/01/2012";
        csvRow.age = "1y 2m 10d";
        PatientProfileRequest patientProfileRequest = new PatientRequestMapper().mapPatient(csvRow, null, null, false);
        assertEquals("2010-11-20", patientProfileRequest.getPatient().getPerson().getBirthdate());

        csvRow.visit_date = "22/12/2014";
        csvRow.age = "22m 400d";
        patientProfileRequest = new PatientRequestMapper().mapPatient(csvRow, null, null, false);
        assertEquals("2012-01-17", patientProfileRequest.getPatient().getPerson().getBirthdate());
    }

    @Test
    public void shouldMapPrefix() throws ParseException {
        SearchCSVRow csvRow = TestUtils.searchCsvBuilder();
        csvRow.prefix = "prefix";
        PatientProfileRequest patientProfileRequest = new PatientRequestMapper().mapPatient(csvRow, null, null, false);
        assertEquals("prefix", patientProfileRequest.getPatient().getPerson().getNames().get(0).getPrefix());
    }

    @Test
    public void shouldNotMapNullValueOfPersonAttribute() throws ParseException {
        SearchCSVRow csvRow = TestUtils.searchCsvBuilder();
        csvRow.middleName = "";
        csvRow.mobileNumber = null;
        AllPatientAttributeTypes allPatientAttributeTypes = new AllPatientAttributeTypes();
        allPatientAttributeTypes.addPersonAttributeType("givenNameLocal", "givenNameLocal-uuid");
        allPatientAttributeTypes.addPersonAttributeType("middleNameLocal", "middleNameLocal-uuid");
        allPatientAttributeTypes.addPersonAttributeType("familyNameLocal", "familyNameLocal-uuid");
        allPatientAttributeTypes.addPersonAttributeType("Mobile", "Mobile-uuid");
        PatientProfileRequest patientProfileRequest = new PatientRequestMapper().mapPatient(csvRow, null, allPatientAttributeTypes, true);

        assertEquals(2, patientProfileRequest.getPatient().getPerson().getAttributes().size());
    }

    @Test
    public void shouldMapBirthdateFromPersonResponseIfUpdatingPatientHasNullBirthdate() {
        String birthdate = "2013-06-10T00:00:00.000+0530";
        Boolean birthdateEstimated = true;
        SearchCSVRow csvRow = TestUtils.searchCsvBuilder();
        csvRow.age = "";
        PatientResponse patientResponse = new PatientResponse();
        PersonResponse person = new PersonResponse();
        person.setBirthdate(birthdate);
        person.setBirthdateEstimated(birthdateEstimated);
        patientResponse.setPerson(person);

        PatientProfileRequest patientProfileRequest = new PatientRequestMapper().mapPatientForUpdate(csvRow, patientResponse, null, true);

        assertEquals(birthdate, patientProfileRequest.getPatient().getPerson().getBirthdate());
        assertTrue(patientProfileRequest.getPatient().getPerson().isBirthdateEstimated());
    }
}
