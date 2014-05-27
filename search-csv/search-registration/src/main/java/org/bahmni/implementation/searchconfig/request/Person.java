package org.bahmni.implementation.searchconfig.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Person {
    private List<Name> names = new ArrayList<Name>();
    private List<PatientAttribute> attributes = new ArrayList<PatientAttribute>();
    private List<PatientAddress> addresses = new ArrayList<PatientAddress>();
    private String birthdate;
    private boolean birthdateEstimated;
    private String gender;

    public void addName(Name patientName) {
        this.names.add(patientName);
    }
}
