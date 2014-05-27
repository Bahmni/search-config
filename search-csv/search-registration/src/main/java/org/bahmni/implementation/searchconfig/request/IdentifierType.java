package org.bahmni.implementation.searchconfig.request;

import lombok.Data;

@Data
public class IdentifierType {
    String name;

    public IdentifierType(String name) {
        this.name = name;
    }
}
