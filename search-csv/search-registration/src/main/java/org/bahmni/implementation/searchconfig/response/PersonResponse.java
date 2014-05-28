package org.bahmni.implementation.searchconfig.response;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonResponse {
    String uuid;
    PersonFieldResponse preferredAddress;
    PersonFieldResponse preferredName;

    public void setPreferredNameUuid(String uuid){
        PersonFieldResponse personFieldResponse = new PersonFieldResponse();
        personFieldResponse.setUuid(uuid);
        this.preferredName = personFieldResponse;
    }

    public void setPreferredAddressUuid(String uuid) {
        PersonFieldResponse personFieldResponse = new PersonFieldResponse();
        personFieldResponse.setUuid(uuid);
        this.preferredAddress = personFieldResponse;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class PersonFieldResponse {
        String uuid;
        Boolean preferred;
    }

}
