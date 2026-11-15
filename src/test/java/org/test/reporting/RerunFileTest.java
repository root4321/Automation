package org.test.reporting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RerunFileTest {
    @Test
    void readsScenarioLinesAndWholeFeatures(@TempDir Path dir) throws IOException {
        Path file = Files.writeString(dir.resolve("failed.txt"),
                "classpath:features/AddPlace.feature:5:9\n\nclasspath:features/GetPlace.feature\n");

        List<RerunFile.Entry> entries = RerunFile.read(file);

        assertEquals(List.of(
                new RerunFile.Entry("classpath:features/AddPlace.feature", List.of(5, 9)),
                new RerunFile.Entry("classpath:features/GetPlace.feature", List.of())), entries);
        assertEquals(3, RerunFile.scenarioCount(entries));
    }

    @Test
    void missingOrEmptyFileMeansNothingFailed(@TempDir Path dir) throws IOException {
        assertTrue(RerunFile.read(dir.resolve("absent.txt")).isEmpty());
        assertTrue(RerunFile.read(Files.writeString(dir.resolve("empty.txt"), "")).isEmpty());
    }

    @Test
    void selectsEachFailedScenarioLine() {
        List<RerunFile.Entry> entries = List.of(new RerunFile.Entry("classpath:features/A.feature", List.of(5, 9)));

        assertEquals(2, FailedScenarioExecutor.selectors(entries).size());
    }
}
