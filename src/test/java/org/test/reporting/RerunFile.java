package org.test.reporting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads the file written by Cucumber's {@code rerun} plugin: one feature per line, e.g. {@code classpath:features/A.feature:5:9}. */
public final class RerunFile {
    private static final Pattern ENTRY = Pattern.compile("^(.+?\\.feature)((?::\\d+)*)$");

    private RerunFile() {
    }

    /** Feature location (without line numbers) and the lines of the failed scenarios; no lines means the whole feature. */
    public record Entry(String uri, List<Integer> lines) {
        int scenarioCount() {
            return Math.max(1, lines.size());
        }
    }

    public static List<String> readLines(Path file) throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }
        return Files.readAllLines(file).stream().map(String::trim).filter(line -> !line.isEmpty()).toList();
    }

    public static List<Entry> read(Path file) throws IOException {
        return readLines(file).stream().map(RerunFile::parse).toList();
    }

    static Entry parse(String line) {
        Matcher matcher = ENTRY.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unrecognised rerun entry: " + line);
        }
        List<Integer> lines = matcher.group(2).isEmpty() ? List.of()
                : java.util.Arrays.stream(matcher.group(2).substring(1).split(":")).map(Integer::valueOf).toList();
        return new Entry(matcher.group(1), lines);
    }

    public static int scenarioCount(List<Entry> entries) {
        return entries.stream().mapToInt(Entry::scenarioCount).sum();
    }
}
