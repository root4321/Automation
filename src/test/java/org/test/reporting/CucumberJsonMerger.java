package org.test.reporting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Merges the Cucumber JSON files of the first run and of every retry into one file. A scenario that was
 * executed again is replaced by its latest result, so the report shows each scenario exactly once.
 */
public final class CucumberJsonMerger {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CucumberJsonMerger() {
    }

    /** Merges {@code attempt-*.json} in name order (first run first) into {@code output}; returns the number of files merged. */
    public static int mergeAttempts(Path attemptsDirectory, Path output) throws IOException {
        List<Path> files;
        try (Stream<Path> listing = Files.exists(attemptsDirectory) ? Files.list(attemptsDirectory) : Stream.empty()) {
            files = listing.filter(file -> file.getFileName().toString().matches("attempt-\\d+\\.json"))
                    .sorted().toList();
        }
        if (files.isEmpty()) {
            return 0;
        }
        Files.createDirectories(output.toAbsolutePath().getParent());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(output.toFile(), merge(files));
        return files.size();
    }

    static ArrayNode merge(List<Path> files) throws IOException {
        Map<String, ObjectNode> features = new LinkedHashMap<>();
        Map<String, Map<String, List<JsonNode>>> unitsByFeature = new LinkedHashMap<>();
        for (Path file : files) {
            if (Files.size(file) == 0) {
                continue;
            }
            for (JsonNode feature : MAPPER.readTree(file.toFile())) {
                String key = feature.path("uri").asText(feature.path("id").asText());
                features.putIfAbsent(key, feature.deepCopy());
                Map<String, List<JsonNode>> units = unitsByFeature.computeIfAbsent(key, k -> new LinkedHashMap<>());
                units.putAll(scenarioUnits(feature));
            }
        }
        ArrayNode merged = MAPPER.createArrayNode();
        features.forEach((key, feature) -> {
            ArrayNode elements = MAPPER.createArrayNode();
            unitsByFeature.get(key).values().forEach(unit -> unit.forEach(elements::add));
            feature.set("elements", elements);
            merged.add(feature);
        });
        return merged;
    }

    /** Groups elements into scenarios keyed by id, each keeping the background that precedes it. */
    private static Map<String, List<JsonNode>> scenarioUnits(JsonNode feature) {
        Map<String, List<JsonNode>> units = new LinkedHashMap<>();
        JsonNode background = null;
        for (JsonNode element : feature.path("elements")) {
            if ("background".equals(element.path("type").asText())) {
                background = element;
                continue;
            }
            List<JsonNode> unit = new ArrayList<>();
            if (background != null) {
                unit.add(background);
                background = null;
            }
            unit.add(element.deepCopy());
            units.put(element.path("id").asText(element.path("name").asText() + ":" + element.path("line").asText()), unit);
        }
        return units;
    }
}
