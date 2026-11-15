package org.test.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.test.config.ApiProperties;
import org.test.model.Place;
import org.test.support.PlaceApiSupport;

import java.io.IOException;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

public class AddPlaceSteps {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PlaceApiSupport api;
    private final ApiProperties properties;
    private String requestBody;
    private String apiKey;
    private Response response;

    public AddPlaceSteps(PlaceApiSupport api, ApiProperties properties) {
        this.api = api;
        this.properties = properties;
        this.apiKey = properties.getKey();
    }

    @Given("a valid add place request with {double}, {double}, {int}, {string}, {string}, {string}, and {string}")
    public void aValidAddPlaceRequest(double latitude, double longitude, int accuracy, String name,
                                      String phone, String address, String types) throws IOException {
        requestBody = objectMapper.writeValueAsString(
                api.addPlacePayload(latitude, longitude, accuracy, name, phone, address, types));
    }

    @Given("the API key is {string}")
    public void theApiKeyIs(String key) {
        apiKey = key;
    }

    @Given("the API key is omitted")
    public void theApiKeyIsOmitted() {
        apiKey = null;
    }

    @Given("the request body is {string}")
    public void theRequestBodyIs(String body) {
        requestBody = body;
    }

    @Given("the place name is omitted")
    public void thePlaceNameIsOmitted() throws IOException {
        ObjectNode payload = (ObjectNode) objectMapper.readTree(requestBody);
        payload.remove("name");
        requestBody = objectMapper.writeValueAsString(payload);
    }

    @Given("the coordinates are latitude {double} and longitude {double}")
    public void theCoordinatesAre(double latitude, double longitude) throws IOException {
        Place payload = objectMapper.readValue(requestBody, Place.class);
        payload.getLocation().setLat(latitude);
        payload.getLocation().setLng(longitude);
        requestBody = objectMapper.writeValueAsString(payload);
    }

    @When("the add place request is sent")
    public void theAddPlaceRequestIsSent() {
        RequestSpecification request = api.withKey(api.request(), apiKey).body(requestBody);
        response = request.when()
                .post(properties.getEndpoints().getAdd());
    }

    @Then("the place is added successfully")
    public void thePlaceIsAddedSuccessfully() {
        response.then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("OK"))
                .body("scope", equalTo("APP"))
                .body("place_id", not(isEmptyOrNullString()))
                .body("reference", not(isEmptyOrNullString()))
                .body("id", not(isEmptyOrNullString()));
    }

    @Then("the request is rejected with a client error")
    public void theRequestIsRejectedWithAClientError() {
        response.then()
                .log().ifValidationFails()
                .statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)));
    }
}
