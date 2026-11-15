package org.test.reporting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CucumberJsonMergerTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static String feature(String... scenarios) {
        return "[{\"uri\":\"classpath:features/A.feature\",\"id\":\"a\",\"elements\":[" + String.join(",", scenarios) + "]}]";
    }

    private static String background() {
        return "{\"id\":\"a;background\",\"type\":\"background\",\"steps\":[]}";
    }

    private static String scenario(String id, String status) {
        return "{\"id\":\"" + id + "\",\"type\":\"scenario\",\"steps\":[{\"result\":{\"status\":\"" + status + "\"}}]}";
    }

    @Test
    void latestResultOfARerunScenarioReplacesTheEarlierOne(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("attempt-00.json"),
                feature(background(), scenario("a;one", "failed"), background(), scenario("a;two", "passed")));
        Files.writeString(dir.resolve("attempt-01.json"), feature(background(), scenario("a;one", "passed")));
        Path output = dir.resolve("out/cucumber.json");

        assertEquals(2, CucumberJsonMerger.mergeAttempts(dir, output));

        JsonNode elements = MAPPER.readTree(output.toFile()).get(0).get("elements");
        assertEquals(4, elements.size());
        assertEquals("background", elements.get(0).get("type").asText());
        assertEquals("a;one", elements.get(1).get("id").asText());
        assertEquals("passed", elements.get(1).get("steps").get(0).get("result").get("status").asText());
        assertEquals("a;two", elements.get(3).get("id").asText());
    }

    @Test
    void doesNothingWithoutAttemptFiles(@TempDir Path dir) throws IOException {
        assertEquals(0, CucumberJsonMerger.mergeAttempts(dir, dir.resolve("cucumber.json")));
        assertEquals(false, Files.exists(dir.resolve("cucumber.json")));
    }
}
