package org.test.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Component;
import org.test.config.ApiProperties;
import org.test.model.Place;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static io.restassured.RestAssured.given;

@Component
public class PlaceApiSupport {
    private final ApiProperties properties;
    private final ObjectMapper objectMapper;

    public PlaceApiSupport(ApiProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
    }

    public RequestSpecification request() {
        return given()
                .filter(new ApiLoggingFilter())
                .baseUri(properties.getBaseUrl())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    public RequestSpecification withKey(RequestSpecification request, String key) {
        return key == null ? request : request.queryParam("key", key);
    }

    public RequestSpecification withPlaceId(RequestSpecification request, String placeId) {
        return placeId == null ? request : request.queryParam("place_id", placeId);
    }

    public ObjectNode placeIdBody(String placeId) {
        ObjectNode body = objectMapper.createObjectNode();
        if (placeId != null) {
            body.put("place_id", placeId);
        }
        return body;
    }

    public ObjectNode updateBody(String placeId, String address, boolean addressIncluded) {
        ObjectNode body = placeIdBody(placeId);
        if (addressIncluded) {
            body.put("address", address);
        }
        body.put("key", properties.getKey());
        return body;
    }

    public Place addPlacePayload(double latitude, double longitude, int accuracy, String name,
                                 String phone, String address, String types) throws IOException {
        try (InputStream input = PlaceApiSupport.class.getResourceAsStream("/Payload/AddPlace.json")) {
            if (input == null) {
                throw new IOException("Missing request payload: /Payload/AddPlace.json");
            }
            Place place = objectMapper.readValue(input, Place.class);
            place.getLocation().setLat(latitude);
            place.getLocation().setLng(longitude);
            place.setAccuracy(accuracy);
            place.setName(name);
            place.setPhoneNumber(phone);
            place.setAddress(address);
            place.setTypes(Arrays.stream(types.split(";", -1)).map(String::trim).toList());
            return place;
        }
    }
}
