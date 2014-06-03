package org.bahmni.implementation.searchconfig.request;

import java.util.List;

public class CreateVisitRequest {
    String patientUuid;
    String visitTypeUuid;
    String encounterTypeUuid;
    private final String visitDateTime;
    List<ProviderRequest> provider;

    public CreateVisitRequest(String savedPatientUuid, String encounterTypeUuid, String visitDateTime) {

        patientUuid = savedPatientUuid;
        this.encounterTypeUuid = encounterTypeUuid;
        this.visitDateTime = visitDateTime;
    }

    public class ProviderRequest{
        String uuid;
    }
}
