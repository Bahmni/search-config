package org.bahmni.implementation.searchconfig.request;

import lombok.Data;

@Data
public class PatientAddress {
    private String uuid;
    private String address3;
    private String cityVillage;
    private String countyDistrict;
    private String stateProvince;
    private String country;

    public PatientAddress(String uuid, String address3, String cityVillage, String countyDistrict, String stateProvince, String country) {
        this.uuid = uuid;
        this.address3 = address3;
        this.cityVillage = cityVillage;
        this.countyDistrict = countyDistrict;
        this.stateProvince = stateProvince;
        this.country = country;
    }

    public PatientAddress(String address3, String cityVillage, String countyDistrict, String stateProvince, String country) {
        this.address3 = address3;
        this.cityVillage = cityVillage;
        this.countyDistrict = countyDistrict;
        this.stateProvince = stateProvince;
        this.country = country;
    }
}
