package org.test.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.java.After;
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
import static org.junit.Assert.assertEquals;

public class PlaceOperationsSteps {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PlaceApiSupport api;
    private final ApiProperties properties;
    private String createdPlaceId;
    private Place createdPlace;
    private String requestedPlaceId;
    private String address;
    private boolean addressIncluded = true;
    private boolean createdPlaceDeleted;
    private Response response;

    public PlaceOperationsSteps(PlaceApiSupport api, ApiProperties properties) {
        this.api = api;
        this.properties = properties;
        this.address = properties.getDefaultUpdateAddress();
    }

    @Given("an existing place with {double}, {double}, {int}, {string}, {string}, {string}, and {string}")
    public void anExistingPlace(double latitude, double longitude, int accuracy, String name,
                                String phone, String address, String types) throws IOException {
        createdPlace = api.addPlacePayload(latitude, longitude, accuracy, name, phone, address, types);
        Response created = api.withKey(api.request(), properties.getKey())
                .body(objectMapper.writeValueAsString(createdPlace))
                .post(properties.getEndpoints().getAdd());
        created.then().log().ifValidationFails()
                .statusCode(200)
                .body("status", equalTo("OK"))
                .body("place_id", not(isEmptyOrNullString()));
        createdPlaceId = created.jsonPath().getString("place_id");
        requestedPlaceId = createdPlaceId;
    }

    @Given("the place identifier is {string}")
    public void thePlaceIdentifierIs(String placeId) {
        requestedPlaceId = placeId;
    }

    @Given("the place identifier is omitted")
    public void thePlaceIdentifierIsOmitted() {
        requestedPlaceId = null;
    }

    @Given("the update address is {string}")
    public void theUpdateAddressIs(String value) {
        address = value;
    }

    @Given("the update address is omitted")
    public void theUpdateAddressIsOmitted() {
        addressIncluded = false;
    }

    @Given("the existing place has been deleted")
    public void theExistingPlaceHasBeenDeleted() {
        deletePlace();
        thePlaceIsDeletedSuccessfully();
    }

    @When("the place is retrieved")
    public void thePlaceIsRetrieved() {
        response = getPlace(requestedPlaceId);
    }

    @When("the place address is updated")
    public void thePlaceAddressIsUpdated() {
        ObjectNode body = api.updateBody(requestedPlaceId, address, addressIncluded);
        RequestSpecification request = api.withKey(api.request(), properties.getKey());
        response = api.withPlaceId(request, requestedPlaceId)
                .body(body.toString()).put(properties.getEndpoints().getUpdate());
    }

    @When("the place is deleted")
    public void deletePlace() {
        ObjectNode body = api.placeIdBody(requestedPlaceId);
        response = delete(body);
        if (createdPlaceId != null && createdPlaceId.equals(requestedPlaceId)
                && response.statusCode() == 200
                && "OK".equals(response.jsonPath().getString("status"))) {
            createdPlaceDeleted = true;
        }
    }

    @Then("the response contains the original place details")
    public void theResponseContainsTheOriginalPlaceDetails() throws IOException {
        response.then().log().ifValidationFails().statusCode(200).contentType(ContentType.JSON);
        JsonNode expected = objectMapper.valueToTree(createdPlace);
        JsonNode actual = objectMapper.readTree(response.asString());
        assertEquals("GET response differs from the documented place details", expected, actual);
    }

    @Then("the address is updated successfully")
    public void theAddressIsUpdatedSuccessfully() {
        response.then().log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("msg", equalTo("Address successfully updated"));
        getPlace(createdPlaceId).then().log().ifValidationFails()
                .statusCode(200)
                .body("address", equalTo(address));
    }

    @Then("the place is deleted successfully")
    public void thePlaceIsDeletedSuccessfully() {
        response.then().log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("OK"));
        getPlace(createdPlaceId).then().log().ifValidationFails().statusCode(404);
    }

    @Then("the place operation is rejected with a client error")
    public void thePlaceOperationIsRejectedWithAClientError() {
        response.then().log().ifValidationFails()
                .statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)));
    }

    @After("@get or @put or @delete")
    public void cleanUpCreatedPlace() {
        if (createdPlaceId != null && !createdPlaceDeleted) {
            ObjectNode body = api.placeIdBody(createdPlaceId);
            delete(body).then().log().ifValidationFails()
                    .statusCode(200)
                    .body("status", equalTo("OK"));
        }
    }

    private Response getPlace(String placeId) {
        RequestSpecification request = api.withKey(api.request(), properties.getKey());
        return api.withPlaceId(request, placeId).get(properties.getEndpoints().getGet());
    }

    private Response delete(ObjectNode body) {
        return api.withKey(api.request(), properties.getKey())
                .body(body.toString())
                .post(properties.getEndpoints().getDelete());
    }
}
