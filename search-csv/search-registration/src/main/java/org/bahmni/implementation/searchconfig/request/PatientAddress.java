package org.bahmni.implementation.searchconfig.request;

import lombok.Data;

@Data
public class PatientAddress {
    private String uuid;
    private String address1;
    private String address2;
    private String address3;
    private String cityVillage;
    private String stateProvince;
    private String country;

    public PatientAddress(String address1, String address2, String address3, String cityVillage, String stateProvince, String country) {
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.cityVillage = cityVillage;
        this.stateProvince = stateProvince;
        this.country = country;
    }

    public PatientAddress(String uuid, String address1, String address2, String address3, String cityVillage, String stateProvince, String country) {
        this.uuid = uuid;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.cityVillage = cityVillage;
        this.stateProvince = stateProvince;
        this.country = country;
    }
}
