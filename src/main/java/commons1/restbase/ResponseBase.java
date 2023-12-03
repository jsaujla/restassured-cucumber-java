package commons.restbase;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.ResponseSpecification;

/**
 * This class provides a base for handling REST responses.
 */
public class ResponseBase {

    /**
     * ThreadLocal variable to hold the ResponseSpecification for each thread.
     */
    private final ThreadLocal<ResponseSpecification> responseSpecification = new ThreadLocal<>();

    /**
     * Constructor to initialize the ResponseSpecification.
     */
    public ResponseBase() {
        createResponseSpecification();
    }

    /**
     * Getter method for the ResponseSpecification.
     * @return ResponseSpecification for the current thread.
     */
    public ResponseSpecification getResponseSpecification() {
        return responseSpecification.get();
    }

    /**
     * Method to create and set the ResponseSpecification for the current thread.
     */
    private void createResponseSpecification() {
        ResponseSpecBuilder responseSpecBuilder = new ResponseSpecBuilder().log(LogDetail.ALL);
        responseSpecification.set(responseSpecBuilder.build());
    }
}
