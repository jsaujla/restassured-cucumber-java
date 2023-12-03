package com.spotify.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.pojo.playlists.Playlists;
import com.spotify.pojo.playlists.error.ErrorRoot;
import io.restassured.response.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class is responsible for initializing page object classes and providing the page objects to step classes.
 * All page object classes should be initialized through this class.
 */
public class PojoInitializer {

    //********** OBJECT DECLARATION **********

//    /**
//     * Constructor to initialize the PageInitializer class.
//     *
//     * @param dependencyContainer An instance of the DependencyContainer class
//     */
//    public PojoInitializer(DependencyContainer dependencyContainer) {}

    protected Playlists getRequestPlaylists(String jsonFilePath) throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonString, Playlists.class);
    }

    protected Playlists getResponsePlaylists(Response response) {
        return response.as(Playlists.class);
    }

    protected ErrorRoot getResponseErrorRoot(Response response) {
        return response.as(ErrorRoot.class);
    }

}
