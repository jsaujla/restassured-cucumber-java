package commons.restbase;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Class for managing request specifications in RestAssured.
 * Each thread gets its own instance of RequestSpecification.
 */
public class RequestBase {

    /**
     * Base URI for the requests.
     */
    private final String baseUri;

    /**
     * Builder for the request specifications.
     */
    private RequestSpecBuilder requestSpecBuilder;

    /**
     * ThreadLocal variable to hold the RequestSpecification for each thread.
     */
    private final ThreadLocal<RequestSpecification> requestSpecification = new ThreadLocal<>();

    /**
     * Constructor to initialize the RequestSpecification and its builder.
     * @param baseUri Base URI for the requests.
     */
    public RequestBase(String baseUri) {
        this.baseUri = baseUri;
        createRequestBuilder();
        createRequestSpecification();
    }

    /**
     * Getter method for the RequestSpecification.
     * @return RequestSpecification for the current thread.
     */
    public RequestSpecification getRequestSpecification() {
        return requestSpecification.get();
    }

    /**
     * Method to reset the RequestSpecification for the current thread.
     */
    public void resetRequestSpecification() {
        createRequestSpecification();
    }

    /**
     * Method to create and set the RequestSpecification for the current thread.
     */
    private void createRequestSpecification() {
        requestSpecification.set(RestAssured.given().spec(requestSpecBuilder.build()).baseUri(baseUri));
    }

    /**
     * Method to create and set the RequestSpecBuilder with logging filters.
     */
    private void createRequestBuilder() {
        PrintStream logFile = null;
        PrintStream errorLogFile = null;
        try {
            String threadName = Thread.currentThread().getName();
            logFile = new PrintStream("target/restassured-all-log-" + threadName + "log");
            errorLogFile = new PrintStream("target/restassured-error-log-" + threadName + "log");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        requestSpecBuilder = new RequestSpecBuilder().log(LogDetail.ALL).
                addFilter(new RequestLoggingFilter(LogDetail.ALL, logFile)).
                addFilter(new ResponseLoggingFilter(LogDetail.ALL, logFile)).
                addFilter(new ErrorLoggingFilter(errorLogFile));
    }
}
