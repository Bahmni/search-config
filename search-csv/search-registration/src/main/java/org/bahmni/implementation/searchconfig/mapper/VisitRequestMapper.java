package org.bahmni.implementation.searchconfig.mapper;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.implementation.searchconfig.SearchCSVRow;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class VisitRequestMapper {

    private final String migratorProviderUuid;
    private final String opdVisitTypeUuid;
    private String registrationEncounterTypeUuid;
    private String registrationFeeConceptUuid;

    public VisitRequestMapper(String migratorProviderUuid, String opdVisitTypeUuid, String registrationEncounterTypeUuid, String RegistrationFeeConceptUuid) {
        this.migratorProviderUuid = migratorProviderUuid;
        this.opdVisitTypeUuid = opdVisitTypeUuid;
        this.registrationEncounterTypeUuid = registrationEncounterTypeUuid;
        registrationFeeConceptUuid = RegistrationFeeConceptUuid;
    }

    public BahmniEncounterTransaction mapVisitRequest(String patientUuid, String encounterTypeUuid, Date visitDateTime, SearchCSVRow csvRow, boolean fromOldCaseNumber) throws java.text.ParseException {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setEncounterTypeUuid(encounterTypeUuid);
        bahmniEncounterTransaction.setProviders(getMigratorProviders());
        bahmniEncounterTransaction.setPatientUuid(patientUuid);
        bahmniEncounterTransaction.setVisitTypeUuid(opdVisitTypeUuid);
        bahmniEncounterTransaction.setEncounterDateTime(visitDateTime);
        if(encounterTypeUuid.equals(registrationEncounterTypeUuid) && !fromOldCaseNumber){
            addObservations(bahmniEncounterTransaction, csvRow);
        }
        return bahmniEncounterTransaction;
    }

    private void addObservations(BahmniEncounterTransaction bahmniEncounterTransaction, SearchCSVRow csvRow) {
        if(StringUtils.isNotEmpty(csvRow.fees)){
            EncounterTransaction.Concept registrationFeeConcept = new EncounterTransaction.Concept();
            registrationFeeConcept.setUuid(registrationFeeConceptUuid);
            EncounterTransaction.Observation observation = new EncounterTransaction.Observation();
            observation.setConcept(registrationFeeConcept);
            observation.setValue(csvRow.fees);
            bahmniEncounterTransaction.addObservation(observation);
        }
    }

    private Set<EncounterTransaction.Provider> getMigratorProviders() {
        HashSet<EncounterTransaction.Provider> providers = new HashSet<EncounterTransaction.Provider>();
        EncounterTransaction.Provider migratorProvider = new EncounterTransaction.Provider();
        migratorProvider.setUuid(migratorProviderUuid);
        providers.add(migratorProvider);
        return providers;
    }
}
