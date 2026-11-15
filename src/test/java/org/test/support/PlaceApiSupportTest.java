package org.test.support;

import org.junit.jupiter.api.Test;
import org.test.config.ApiProperties;
import org.test.model.Place;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceApiSupportTest {
    @Test
    void populatesTheTemplateFromScenarioValues() throws IOException {
        Place place = new PlaceApiSupport(new ApiProperties()).addPlacePayload(
                12.5, -40.5, 25, "Example", "123", "Test address", "park;shop");

        assertEquals(12.5, place.getLocation().getLat());
        assertEquals(-40.5, place.getLocation().getLng());
        assertEquals(25, place.getAccuracy());
        assertEquals("Example", place.getName());
        assertEquals("123", place.getPhoneNumber());
        assertEquals("Test address", place.getAddress());
        assertEquals(List.of("park", "shop"), place.getTypes());
        assertEquals("http://google.com", place.getWebsite());
        assertEquals("French-IN", place.getLanguage());
    }
}
