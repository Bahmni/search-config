package org.bahmni.implementation.searchconfig.request;

import lombok.Data;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Person {
    String uuid;
    private List<Name> names = new ArrayList<Name>();
    private List<PatientAttribute> attributes = new ArrayList<PatientAttribute>();
    private List<PatientAddress> addresses = new ArrayList<PatientAddress>();
    private String birthdate;
    private boolean birthdateEstimated;
    private String gender;
    private Date personDateCreated;

    public void addName(Name patientName) {
        this.names.add(patientName);
    }

    public String getPersonDateCreated() {
        if (personDateCreated != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return simpleDateFormat.format(personDateCreated);
        }
        return null;
    }
}
