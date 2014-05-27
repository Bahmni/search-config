package org.bahmni.implementation.searchconfig.request;

import lombok.Data;

@Data
public class PatientAttribute {
    private String attributeType;
    private String value;

    public PatientAttribute(String attributeType, String value) {
        this.attributeType = attributeType;
        this.value = value;
    }
}
