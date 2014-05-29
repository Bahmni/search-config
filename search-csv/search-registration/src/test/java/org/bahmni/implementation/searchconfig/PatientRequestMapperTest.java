package org.bahmni.implementation.searchconfig;

import org.bahmni.implementation.searchconfig.request.Patient;
import org.bahmni.implementation.searchconfig.request.PatientProfileRequest;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class PatientRequestMapperTest {

    @Test
    public void shouldMapNAmeFromCsvRowWhenAllPartsOfNameExist(){
        SearchCSVRow csvRow = new SearchCSVRow();
        String firstName = "first";
        String middleName = "middle";
        String lastName = "last";
        csvRow.firstName = firstName;
        csvRow.middleName = middleName;
        csvRow.lastName = lastName;

        PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapPatient(csvRow, false);
        Patient patient = patientProfileRequest.getPatient();

        assertEquals(1, patient.getPerson().getNames().size());
        assertEquals(firstName, patient.getPerson().getNames().get(0).getGivenName());
        assertEquals(middleName, patient.getPerson().getNames().get(0).getMiddleName());
        assertEquals(lastName, patient.getPerson().getNames().get(0).getFamilyName());
    }

    @Test
    public void shouldMapNAme_ByMappingMiddleNameAsLastName_WhenLastNameIsEmpty(){
        SearchCSVRow csvRow = new SearchCSVRow();
        String firstName = "first";
        String middleName = "middle";
        String lastName = "";
        csvRow.firstName = firstName;
        csvRow.middleName = middleName;
        csvRow.lastName = lastName;

        PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapPatient(csvRow, false);
        Patient patient = patientProfileRequest.getPatient();

        assertEquals(1, patient.getPerson().getNames().size());
        assertEquals(firstName, patient.getPerson().getNames().get(0).getGivenName());
        assertEquals("", patient.getPerson().getNames().get(0).getMiddleName());
        assertEquals(middleName, patient.getPerson().getNames().get(0).getFamilyName());
    }

    @Test
    public void shouldMapOldCaseNumberAsPatientIdentifier(){
        SearchCSVRow csvRow = new SearchCSVRow();
        String oldCaseNumber = "1234/12";
        csvRow.oldCaseNo = oldCaseNumber;

        PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapPatient(csvRow, true);
        Patient patient = patientProfileRequest.getPatient();

        assertEquals("SEA" + oldCaseNumber, patient.getIdentifiers().get(0).getIdentifier());
    }

    @Test
    public void shouldMapNewCaseNumberAsPatientIdentifier(){
        SearchCSVRow csvRow = new SearchCSVRow();
        String newCaseNumber = "1234/12";
        csvRow.newCaseNo = newCaseNumber;

        PatientProfileRequest patientProfileRequest = PatientRequestMapper.mapPatient(csvRow, false);
        Patient patient = patientProfileRequest.getPatient();

        assertEquals("SEA" + newCaseNumber, patient.getIdentifiers().get(0).getIdentifier());
    }
}
