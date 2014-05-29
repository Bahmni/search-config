package org.bahmni.implementation.searchconfig.response;

import lombok.Data;

import java.util.List;

@Data
public class PatientListResponse {
    List<PatientResponse> results;
}
