package com.spotify.steps;

import com.spotify.pojo.playlists.Playlists;
import com.spotify.pojo.playlists.error.ErrorRoot;
import com.spotify.oauth.TokenManager;
import com.spotify.config.ConfigLoader;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import commons.restbase.RequestBase;
import commons.restbase.ResponseBase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This class contains the implementation of register step definitions that correspond to feature files.
 * It extends the PageInitializer class to access the page objects and driver.
 */
public class PlaylistsSteps {

    private final RequestBase requestBase;
    private final ResponseBase responseBase;
    private Response response;

    private final PojoInitializer pojoInitializer;
    private Playlists requestPlaylists;

    String userId;
    String playlistIdFromPostRequest;

    /**
     * Constructor to initialize the RegisterSteps class.
     *
     * @param dependencyContainer An instance of the DependencyContainer class
     */
    public PlaylistsSteps(DependencyContainer dependencyContainer) {
        requestBase = dependencyContainer.requestBase;
        responseBase = dependencyContainer.responseBase;
        response = dependencyContainer.response;
        pojoInitializer = dependencyContainer.pojoInitializer;
    }

    //********** STEP DEFINITION METHODS **********

    @Given("An authorized user is available")
    public void an_authorized_user_is_available() {
        //userId = "31ddawwwfblnhu5okxeb4geahrmm";
        userId = ConfigLoader.getInstance().getUserId();
        // No implementation required
    }

    @When("With request headers")
    public void with_request_headers(DataTable dataTable) {
        List<List<String>> table = dataTable.asLists(String.class);
        if (table.size() != 2) {
            throw new IllegalArgumentException("DataTable must have two rows");
        }
        Map<String, String> header = new HashMap<>();
        for(int i=0; i<table.get(0).size(); i++) {
            String resolvedRowTwoDataWithConfigFile = ConfigLoader.getInstance().replacePlaceholdersWithProperties(table.get(1).get(i));
            String resolvedRowTwoDataWithConfigFileAndAccessToken = resolvedRowTwoDataWithConfigFile;
            if (resolvedRowTwoDataWithConfigFile.contains("{access_token}")) {
                resolvedRowTwoDataWithConfigFileAndAccessToken = resolvedRowTwoDataWithConfigFile.replace("{access_token}", TokenManager.getToken());
            }
            header.put(table.get(0).get(i), resolvedRowTwoDataWithConfigFileAndAccessToken);
        }
        requestBase.getRequestSpecification().headers(header);
    }

    @And("With request body: {string}")
    public void with_request_body(String jsonFilePath, DataTable dataTable) throws IOException {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        requestPlaylists = pojoInitializer.getRequestPlaylists(jsonFilePath);
        requestPlaylists.setName(rows.get(0).get("name"));
        requestPlaylists.setDescription(rows.get(0).get("description"));
        Boolean isPublic = Boolean.parseBoolean(rows.get(0).get("public"));
        requestPlaylists.set_public(isPublic);
        requestBase.getRequestSpecification().body(requestPlaylists);
    }

    @When("User makes a POST request to endpoint: {string}")
    public void user_makes_a_post_request_to_endpoint(String endpoint) {
        String resolvedEndpointWithConfigFile = ConfigLoader.getInstance().replacePlaceholdersWithProperties(endpoint);
        //String actualEndpoint = endpoint.replace("{user_id}", userId);
        response = requestBase.getRequestSpecification().post(resolvedEndpointWithConfigFile).
                then().spec(responseBase.getResponseSpecification()).extract().response();
        requestBase.resetRequestSpecification();
    }

    @When("User makes a GET request to endpoint: {string}")
    public void user_makes_a_get_request_to_endpoint(String endpoint) {
        //String actualEndpoint = endpoint.replace("{playlist_id_from_post_request}", playlistIdFromPostRequest);
        String resolvedEndpointWithConfigFile = ConfigLoader.getInstance().replacePlaceholdersWithProperties(endpoint);
        String resolvedEndpointWithConfigFileAndDataStore = resolvedEndpointWithConfigFile.replace("{playlist_id_from_post_request}", playlistIdFromPostRequest);
        response = requestBase.getRequestSpecification().get(resolvedEndpointWithConfigFileAndDataStore).
                then().spec(responseBase.getResponseSpecification()).extract().response();
        requestBase.resetRequestSpecification();
    }

    @When("User makes a PUT request to endpoint: {string}")
    public void user_makes_a_put_request_to_endpoint(String endpoint) {
        //String actualEndpoint = endpoint.replace("{playlist_id_from_post_request}", playlistIdFromPostRequest);
        String resolvedEndpointWithConfigFile = ConfigLoader.getInstance().replacePlaceholdersWithProperties(endpoint);
        String resolvedEndpointWithConfigFileAndDataStore = endpoint.replace("{playlist_id_from_post_request}", playlistIdFromPostRequest);
        response = requestBase.getRequestSpecification().put(resolvedEndpointWithConfigFileAndDataStore).
                then().spec(responseBase.getResponseSpecification()).extract().response();
        requestBase.resetRequestSpecification();
    }

    @Then("Response status code should be: {int}")
    public void response_status_code_should_be(int statusCode) {
        assertThat(response.getStatusCode(), CoreMatchers.equalTo(statusCode));
    }

    @Then("Response body should match with schema: {string}")
    public void response_body_should_match_with_schema(String expectedSchemaFilePath) throws IOException {
        File jsonSchemaReferenceFile = new File(expectedSchemaFilePath);

        String jsonString = new String(Files.readAllBytes(Paths.get(expectedSchemaFilePath)));

        System.out.println("++++++++++++++++++++++++++++++ " + jsonString);
        System.out.println("++++++++++++++++++++++++++++++ " + response.getBody().prettyPrint());


        JsonSchemaValidator jsonSchemaValidator = JsonSchemaValidator.matchesJsonSchema(jsonSchemaReferenceFile);
        System.out.println("++++++++++++++++++++++++++++++jsonSchemaValidator " + jsonSchemaValidator);
        //assertThat(response.getBody().toString(), JsonSchemaValidator.matchesJsonSchema(jsonString));
        assertThat(response.getBody().toString(), JsonSchemaValidator.matchesJsonSchema(jsonSchemaReferenceFile));

        //assertThat(response.getBody().toString(), JsonSchemaValidator.matchesJsonSchemaInClasspath(expectedSchemaFilePath));
    }

    @And("Response body should contains fields")
    public void response_body_should_contains_fields(DataTable dataTable) {

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);

        //assertThat(response.getBody().path("id"), CoreMatchers.is(statusCode));
//        if(rows.get(0).get("id").equals("NOT_NULL")) {
//            assertThat(response.getBody().path("id"), CoreMatchers.notNullValue());
//        } else if(rows.get(0).get("id").startsWith("{") && rows.get(0).get("id").endsWith("}")) {
//            assertThat(response.getBody().path("id"), CoreMatchers.is(playlistIdFromPostRequest));
//        } else {
//            assertThat(response.getBody().path("id"), CoreMatchers.is(rows.get(0).get("id")));
//        }
//        assertThat(response.getBody().path("name"), CoreMatchers.is(rows.get(0).get("name")));
//        assertThat(response.getBody().path("description"), CoreMatchers.is(rows.get(0).get("description")));
//        assertThat(response.getBody().path("public"), CoreMatchers.is(Boolean.parseBoolean(rows.get(0).get("public"))));
//
//        assertThat(responsePlaylists.getName(), CoreMatchers.is(rows.get(0).get("name")));
//        assertThat(response.getBody().path("description"), CoreMatchers.is(rows.get(0).get("description")));
//        assertThat(response.getBody().path("public"), CoreMatchers.is(Boolean.parseBoolean(rows.get(0).get("public"))));

        Playlists responsePlaylists = pojoInitializer.getResponsePlaylists(response);

        assertThat(responsePlaylists.getName(), CoreMatchers.equalTo(requestPlaylists.getName()));
        assertThat(responsePlaylists.getDescription(), CoreMatchers.equalTo(requestPlaylists.getDescription()));
        assertThat(responsePlaylists.get_public(), CoreMatchers.equalTo(requestPlaylists.get_public()));

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(responsePlaylists.getName()).isEqualTo(requestPlaylists.getName());
        softly.assertThat(responsePlaylists.getDescription()).isEqualTo(requestPlaylists.getDescription());
        softly.assertThat(responsePlaylists.get_public()).isEqualTo(requestPlaylists.get_public());
        softly.assertAll();

    }

    @And("Response body should contains error fields")
    public void response_body_should_contains_error_fields(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);

        ErrorRoot responseErrorRoot = pojoInitializer.getResponseErrorRoot(response);

        SoftAssertions softly = new SoftAssertions();
        int expectedErrorStatus = Integer.parseInt(rows.get(0).get("error.status"));
        softly.assertThat(responseErrorRoot.getError().getStatus()).isEqualTo(expectedErrorStatus);
        softly.assertThat(responseErrorRoot.getError().getMessage()).isEqualTo(rows.get(0).get("error.message"));
        softly.assertAll();

    }

}
