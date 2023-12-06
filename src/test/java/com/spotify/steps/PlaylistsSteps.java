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
import commons.restbase.RequestBase;
import commons.restbase.ResponseBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This class contains the implementation of Playlists step definitions that correspond to feature files.
 */
public class PlaylistsSteps {

    /**
     * Logger object for logging purposes. It's declared as final because it's a constant.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistsSteps.class);

    /**
     * The instance of RequestBase class used to manage request specifications.
     */
    private final RequestBase requestBase;

    /**
     * The instance of ResponseBase class used to manage response specifications.
     */
    private final ResponseBase responseBase;

    /**
     * The instance of Response class used to store the response of the HTTP request.
     */
    private Response response;

    /**
     * The instance of PojoInitializer class used to call pojo classes.
     */
    private final PojoInitializer pojoInitializer;

    private Playlists requestPlaylists;
    private String playlistIdFromPostRequest;

    /**
     * Constructor to initialize the RegisterSteps class.
     *
     * @param dependencyContainer An instance of the DependencyContainer class
     */
    public PlaylistsSteps(DependencyContainer dependencyContainer) {
        LOGGER.info("Constructing PlaylistsSteps");
        requestBase = dependencyContainer.requestBase;
        responseBase = dependencyContainer.responseBase;
        response = dependencyContainer.response;
        pojoInitializer = dependencyContainer.pojoInitializer;
    }

    //********** STEP DEFINITION METHODS **********

    @Given("An authorized user is available")
    public void an_authorized_user_is_available() {
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
        response = requestBase.getRequestSpecification().post(resolvedEndpointWithConfigFile).
                then().spec(responseBase.getResponseSpecification()).extract().response();
        requestBase.resetRequestSpecification();
    }

    @When("User makes a GET request to endpoint: {string}")
    public void user_makes_a_get_request_to_endpoint(String endpoint) {
        String resolvedEndpointWithConfigFile = ConfigLoader.getInstance().replacePlaceholdersWithProperties(endpoint);
        String resolvedEndpointWithConfigFileAndDataStore = resolvedEndpointWithConfigFile.replace("{playlist_id_from_post_request}", playlistIdFromPostRequest);
        response = requestBase.getRequestSpecification().get(resolvedEndpointWithConfigFileAndDataStore).
                then().spec(responseBase.getResponseSpecification()).extract().response();
        requestBase.resetRequestSpecification();
    }

    @When("User makes a PUT request to endpoint: {string}")
    public void user_makes_a_put_request_to_endpoint(String endpoint) {
        String resolvedEndpointWithConfigFile = ConfigLoader.getInstance().replacePlaceholdersWithProperties(endpoint);
        String resolvedEndpointWithConfigFileAndDataStore = resolvedEndpointWithConfigFile.replace("{playlist_id_from_post_request}", playlistIdFromPostRequest);
        response = requestBase.getRequestSpecification().put(resolvedEndpointWithConfigFileAndDataStore).
                then().spec(responseBase.getResponseSpecification()).extract().response();
        requestBase.resetRequestSpecification();
    }

    @Then("Response status code should be: {int}")
    public void response_status_code_should_be(int statusCode) {
        assertThat(response.getStatusCode(), CoreMatchers.equalTo(statusCode));
    }

    @And("Response body should contains valid fields")
    public void response_body_should_contains_valid_fields() {
        Playlists responsePlaylists = pojoInitializer.getResponsePlaylists(response);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(responsePlaylists.getId()).isNotNull();
        softly.assertThat(responsePlaylists.getName()).isEqualTo(requestPlaylists.getName());
        softly.assertThat(responsePlaylists.getDescription()).isEqualTo(requestPlaylists.getDescription());
        softly.assertThat(responsePlaylists.get_public()).isEqualTo(requestPlaylists.get_public());
        softly.assertAll();

        playlistIdFromPostRequest = responsePlaylists.getId();
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

    @Then("Response body should match with schema: {string}")
    public void response_body_should_match_with_schema(String expectedSchemaFilePath) {
        File jsonSchemaReferenceFile = new File(expectedSchemaFilePath);
        assertThat(response.getBody().toString(), JsonSchemaValidator.matchesJsonSchema(jsonSchemaReferenceFile));
    }

}
