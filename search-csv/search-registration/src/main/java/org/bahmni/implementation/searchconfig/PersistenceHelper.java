package org.bahmni.implementation.searchconfig;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.bahmni.implementation.searchconfig.response.PatientListResponse;
import org.bahmni.implementation.searchconfig.response.PatientResponse;
import org.bahmni.openmrsconnector.OpenMRSRESTConnection;
import org.bahmni.openmrsconnector.OpenMRSRestService;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public class PersistenceHelper {
    private static Logger logger = org.apache.log4j.Logger.getLogger(PersistenceHelper.class);
    private static ObjectMapper objectMapper = new ObjectMapper();
    private OpenMRSRESTConnection openMRSRESTConnection;
    private OpenMRSRestService openMRSRestService;

    public PersistenceHelper(OpenMRSRESTConnection openMRSRESTConnection, OpenMRSRestService openMRSRestService) {
        this.openMRSRESTConnection = openMRSRESTConnection;
        this.openMRSRestService = openMRSRestService;
    }

    public PatientResponse getPatientFromOpenmrs(String patientIdentifier) {
        String representation = "custom:(uuid,person:(uuid,preferredAddress:(uuid,preferred),preferredName:(uuid)))";
        String url = openMRSRESTConnection.getRestApiUrl() + "patient?q=" + patientIdentifier + "&v=" + representation;
        ResponseEntity<String> response = getFromOpenmrs(url);
        PatientListResponse patientListResponse = new Gson().fromJson(response.getBody(), PatientListResponse.class);
        if (patientListResponse.getResults().size() > 0) {
            return patientListResponse.getResults().get(0);
        }
        return null;
    }

    public ResponseEntity<String> getFromOpenmrs(String url) {
        HttpEntity httpEntity = new HttpEntity(getHttpHeaders());
        return new RestTemplate().exchange(url, HttpMethod.GET, httpEntity, String.class);
    }

    public JSONObject postToOpenmrs(String url, Object request) throws IOException, ParseException {
        String jsonRequest = objectMapper.writeValueAsString(request);
        if (logger.isDebugEnabled()) logger.debug(jsonRequest);
        HttpHeaders httpHeaders = getHttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<Object>(request, httpHeaders);
        ResponseEntity<String> out = new RestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
        if (logger.isDebugEnabled()) logger.debug(out.getBody());
        JSONObject parsedResponse = (JSONObject) new JSONParser().parse(out.getBody());
        return parsedResponse;
    }

    public String extractErrorMessage(HttpServerErrorException serverErrorException) {
        String responseBody = serverErrorException.getResponseBodyAsString();
        int startIndex = responseBody.indexOf("message");
        int endIndex = responseBody.indexOf(",\"code\"");
        String message = responseBody.substring(startIndex, endIndex);
        //Replacing quotes since the error message will be written to a csv
        return message.replaceAll("\"", "");
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Cookie", "JSESSIONID=" + openMRSRestService.getSessionId());
        return requestHeaders;
    }

}
