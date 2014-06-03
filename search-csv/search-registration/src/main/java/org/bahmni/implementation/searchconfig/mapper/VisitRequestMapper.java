package org.bahmni.implementation.searchconfig.mapper;

import org.bahmni.module.bahmnicore.contract.encounter.request.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class VisitRequestMapper {

    private final String migratorProviderUuid;
    private final String opdVisitTypeUuid;

    public VisitRequestMapper(String migratorProviderUuid, String opdVisitTypeUuid) {
        this.migratorProviderUuid = migratorProviderUuid;
        this.opdVisitTypeUuid = opdVisitTypeUuid;
    }

    public BahmniEncounterTransaction mapVisitRequest(String patientUuid, String encounterTypeUuid, Date visitDateTime) throws java.text.ParseException {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setEncounterTypeUuid(encounterTypeUuid);
        bahmniEncounterTransaction.setProviders(getMigratorProviders());
        bahmniEncounterTransaction.setPatientUuid(patientUuid);
        bahmniEncounterTransaction.setVisitTypeUuid(opdVisitTypeUuid);
        bahmniEncounterTransaction.setEncounterDateTime(visitDateTime);
        return bahmniEncounterTransaction;
    }

    private Set<EncounterTransaction.Provider> getMigratorProviders() {
        HashSet<EncounterTransaction.Provider> providers = new HashSet<EncounterTransaction.Provider>();
        EncounterTransaction.Provider migratorProvider = new EncounterTransaction.Provider();
        migratorProvider.setUuid(migratorProviderUuid);
        providers.add(migratorProvider);
        return providers;
    }
}
