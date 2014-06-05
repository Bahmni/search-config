package org.bahmni.implementation.searchconfig.request;

import lombok.Data;

@Data
public class PatientAttribute {
    private AttributeType attributeType;
    private String value;

    public PatientAttribute(String attributeUuid, String value) {
        this.attributeType = new AttributeType(attributeUuid);
        this.value = value;
    }

    @Data
    public class AttributeType{
        private String uuid;

        public AttributeType(String uuid) {
            this.uuid = uuid;
        }
    }
}
