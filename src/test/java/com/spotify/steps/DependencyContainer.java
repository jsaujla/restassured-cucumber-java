package com.spotify.steps;

import commons.restbase.ResponseBase;
import commons.properties.PropertiesManager;
import io.restassured.response.Response;
import commons.restbase.RequestBase;

/**
 * This class is used to store shared object instances and make them available to Hooks, PageInitializer and Steps classes.
 */
public class DependencyContainer {

    /**
     * The instance of PropertiesManager class used to access and manage test configuration properties.
     */
    protected PropertiesManager propertiesManager;

    /**
     * The instance of RequestBase class used to manage request specifications.
     */
    protected RequestBase requestBase;

    /**
     * The instance of ResponseBase class used to manage response specifications.
     */
    protected ResponseBase responseBase;

    /**
     * The instance of Response class used to store the response of the HTTP request.
     */
    protected Response response;

    /**
     * The instance of PojoInitializer class used to call pojo classes.
     */
    protected PojoInitializer pojoInitializer;

}
