package org.bahmni.implementation.searchconfig.request;

import lombok.Data;

@Data
public class PatientProfileRequest {
    private Patient patient;

    public PatientProfileRequest(Patient patient) {
        this.patient = patient;
    }
}
