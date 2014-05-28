package org.bahmni.implementation.searchconfig.response;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientListResponse {
    List<PatientResponse> results;
}
