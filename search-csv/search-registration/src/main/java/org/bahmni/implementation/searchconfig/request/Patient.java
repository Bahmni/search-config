package org.bahmni.implementation.searchconfig.request;

import lombok.Data;

import java.util.List;

@Data
public class Patient {
    private Person person;
    private List<PatientIdentifier> identifiers;

    public Patient(Person person, List<PatientIdentifier> identifiers) {
        this.person = person;
        this.identifiers = identifiers;
    }
}
