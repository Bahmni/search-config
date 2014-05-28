package org.bahmni.implementation.searchconfig.response;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientResponse {
    String uuid;
    PersonResponse person;
}