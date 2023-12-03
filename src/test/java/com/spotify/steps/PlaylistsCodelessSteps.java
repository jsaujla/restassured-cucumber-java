package com.spotify.steps;

import com.spotify.oauth.TokenManager;
import com.spotify.config.ConfigLoader;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import com.spotify.codeless.DataStoreManager;
import com.spotify.codeless.RequestBodyManager;
import commons.restbase.RequestBase;
import commons.restbase.ResponseBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This class contains the implementation of register step definitions that correspond to feature files.
 * It extends the PageInitializer class to access the page objects and driver.
 */
public class PlaylistsCodelessSteps {

    private final RequestBase requestBase;
    private final ResponseBase responseBase;
    private Response response;

    private  final RequestBodyManager requestBodyManager;
    private final DataStoreManager dataStoreManager;

    String userId;
    //String playlistIdFromPostRequest;

    /**
     * Constructor to initialize the RegisterSteps class.
     *
     * @param dependencyContainer An instance of the DependencyContainer class
     */
    public PlaylistsCodelessSteps(DependencyContainer dependencyContainer) {
        requestBase = dependencyContainer.requestBase;
        responseBase = dependencyContainer.responseBase;
        response = dependencyContainer.response;
        requestBodyManager = dependencyContainer.requestBodyManager;
        dataStoreManager = dependencyContainer.dataStoreManager;
    }

    //********** STEP DEFINITION METHODS **********

    @Given("An authorized user is available")
    public void an_authorized_user_is_available() {
        userId = ConfigLoader.getInstance().getUserId();
    }

    @When("With request headers")
    public void with_request_headers(DataTable dataTable) {
        String accessToken = TokenManager.getToken();;
        String expiredAccessToken = "Bearer BQBCBWthnBmyC4EDzu9uoozidbojbQZN-t4eT5jBr8bWHbL4IGeCAyiaHbtvrRByoPj03NeRm2xCKRvXxE3lQGvPBCh8PaRQfIpsMrTYZwiZ40q3F2t4DoIe4y0QAMb0Fmc8AmMRnVSS08-uMFncAssh-rz8S5_BmNPV3HeLWpZRNfxAfhfmZUWLx0lQDpEvui0M_7fP8cu-YxfQTLDbbe8miHPUCBJzeSvOX7Q2SSLhJ8Sf6GA1jW_LhClKzaDb-Li655rE8mfONcPz";

        List<List<String>> table = dataTable.asLists(String.class);
        if (table.size() != 2) {
            throw new IllegalArgumentException("DataTable must have two rows");
        }
        Map<String, String> header = new HashMap<>();
        for(int i=0; i<table.get(0).size(); i++) {
            String rowTwoData = table.get(1).get(i).replace("{access_token}", accessToken).
                    replace("{expired_access_token}", expiredAccessToken);
            header.put(table.get(0).get(i), rowTwoData);
        }
        requestBase.getRequestSpecification().headers(header);
    }

    @And("With request body: {string}")
    public void with_request_body(String jsonFilePath, DataTable dataTable) {
        List<List<String>> table = dataTable.asLists(String.class);
//        if (table.size() != 2) {
//            throw new IllegalArgumentException("DataTable must have two rows");
//        }

        requestBodyManager.validateTableSize(table);
        JSONObject json = requestBodyManager.readJsonFromFile(jsonFilePath);
        requestBodyManager.updateJsonWithTableData(json, table);
        requestBase.getRequestSpecification().body(json.toString());
    }

    @When("User makes a POST request to endpoint: {string}")
    public void user_makes_a_post_request_to_endpoint(String endpoint) {
        String actualEndpoint = endpoint.replace("{user_id}", userId);
        response = requestBase.getRequestSpecification().post(actualEndpoint).
                then().spec(responseBase.getResponseSpecification()).extract().response();
        //playlistIdFromPostRequest = response.getBody().path("id");
        requestBase.resetRequestSpecification();

    }

    @When("User makes a GET request to endpoint: {string}")
    public void user_makes_a_get_request_to_endpoint(String endpoint) {
        String actualEndpoint = dataStoreManager.resolvePlaceholdersWithData(endpoint);
        response = requestBase.getRequestSpecification().get(actualEndpoint).
                then().spec(responseBase.getResponseSpecification()).extract().response();
        requestBase.resetRequestSpecification();
    }

    @When("User makes a PUT request to endpoint: {string}")
    public void user_makes_a_put_request_to_endpoint(String endpoint) {
        String actualEndpoint = dataStoreManager.resolvePlaceholdersWithData(endpoint);
        response = requestBase.getRequestSpecification().put(actualEndpoint).
                then().spec(responseBase.getResponseSpecification()).extract().response();
        requestBase.resetRequestSpecification();
    }

    @Then("Response status code should be: {int}")
    public void response_status_code_should_be(int statusCode) {
        //assertThat(dependencyContainer.response.getStatusCode(), CoreMatchers.is(statusCode));
        assertThat(response.getStatusCode(), Matchers.equalTo(statusCode));
    }

    @And("Response body should contains fields")
    public void response_body_should_contains_fields(DataTable dataTable) {

        List<List<String>> rows = dataTable.asLists(String.class);
        if (rows.size() != 3) {
            throw new IllegalArgumentException("DataTable must have three rows");
        }

        for(int i=0; i<rows.get(0).size(); i++) {
            String jsonPath = rows.get(0).get(i); // First Row
            Object expectedValue = rows.get(1).get(i); // Second Row
            String expectedValueType = rows.get(2).get(i); // Third Row

            if (expectedValue.equals("NOT_NULL")) {
                assertThat(response.getBody().path(jsonPath), CoreMatchers.notNullValue());
            } else if (expectedValue.equals("NULL") || expectedValue.equals("null")) {
                assertThat(response.getBody().path(jsonPath), CoreMatchers.nullValue());
            } else {
                //expectedValue = convertOrRetrieveExpectedValue(expectedValue, expectedValueType);
                expectedValue = dataStoreManager.convertOrRetrieveExpectedValue(expectedValue, expectedValueType);
                assertThat(response.getBody().path(jsonPath), CoreMatchers.is(expectedValue));
            }
        }
    }

    @And("Store response body value in variable")
    public void store_response_body_value_in_variable(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        if (rows.get(0).size() != 3) {
            throw new IllegalArgumentException("DataTable must have three columns. variableName | variableType | responsePath");
        }
        for(int i=0; i<rows.size(); i++) {
            //storeResponseBodyValue(rows.get(i));
            dataStoreManager.storeResponseBodyValue(rows.get(i), response);
        }
    }

}
