package org.bahmni.implementation.searchconfig.request;

import lombok.Data;

@Data
public class PatientIdentifier {
    String identifier;
    IdentifierType identifierType;
    Boolean preferred;

    public PatientIdentifier(String identifier, IdentifierType identifierType, Boolean preferred) {
        this.identifier = identifier;
        this.identifierType = identifierType;
        this.preferred = preferred;
    }
}
