package com.spotify.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.pojo.playlists.Playlists;
import com.spotify.pojo.playlists.error.ErrorRoot;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class is responsible for initializing page object classes and providing the page objects to step classes.
 * All page object classes should be initialized through this class.
 */
public class PojoInitializer {

    /**
     * Logger object for logging purposes. It's declared as final because it's a constant.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PojoInitializer.class);

    /**
     * This method reads a JSON file from a given path and maps it to a Playlists object.
     *
     * @param jsonFilePath The path to the JSON file.
     * @return A Playlists object that represents the data in the JSON file.
     * @throws IOException If an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read.
     */
    protected Playlists getRequestPlaylists(String jsonFilePath) throws IOException {
        LOGGER.info("Returning Request Playlists object that represents the data in the JSON file '{}'", jsonFilePath);
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonString, Playlists.class);
    }

    /**
     * This method maps the response to a Playlists object.
     *
     * @param response The response to be mapped.
     * @return A Playlists object that represents the data in the response.
     */
    protected Playlists getResponsePlaylists(Response response) {
        LOGGER.info("Returning Response Playlists object that represents the data in the response");
        return response.as(Playlists.class);
    }

    /**
     * This method maps the response to an ErrorRoot object.
     *
     * @param response The response to be mapped.
     * @return An ErrorRoot object that represents the data in the response.
     */
    protected ErrorRoot getResponseErrorRoot(Response response) {
        LOGGER.info("Returning Response ErrorRoot object represents the data in the response");
        return response.as(ErrorRoot.class);
    }

}
