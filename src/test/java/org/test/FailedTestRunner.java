package org.test;

import org.junit.jupiter.api.Test;
import org.test.reporting.CucumberJsonMerger;
import org.test.reporting.FailedScenarioExecutor;
import org.test.reporting.RerunFile;
import org.test.reporting.RetryPolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Executes the scenarios that failed in the earlier {@link TestRunner} run, listed in
 * {@code target/rerun/failed.txt}. Only scenarios that are still failing are executed again, up to
 * {@code -Dretry.count} times (default 3, maximum 4). Finally the results of all attempts are merged into
 * {@code target/cucumber/cucumber.json}, the input of the consolidated Masterthought report.
 * The test fails if scenarios are still failing after the last retry.
 *
 * <p>To run it on its own after a {@code mvn test}: {@code mvn surefire:test@rerun-failed}.
 */
public class FailedTestRunner {
    private static final Path FAILED_SCENARIOS = Path.of(TestRunner.FAILED_SCENARIOS_FILE);
    private static final Path ATTEMPTS = Path.of(TestRunner.ATTEMPTS_DIRECTORY);
    private static final Path CONSOLIDATED_JSON = Path.of("target/cucumber/cucumber.json");

    @Test
    void rerunFailedScenarios() throws IOException {
        int maxRetries = RetryPolicy.fromSystemProperty();
        for (int retry = 1; retry <= maxRetries; retry++) {
            List<RerunFile.Entry> failed = RerunFile.read(FAILED_SCENARIOS);
            if (failed.isEmpty()) {
                break;
            }
            int attempt = nextAttemptNumber();
            System.out.printf("%n===== Retry %d of %d: re-executing %d failed scenario(s) =====%n",
                    retry, maxRetries, RerunFile.scenarioCount(failed));
            Path attemptRerunFile = Path.of("target/rerun", String.format("attempt-%02d.txt", attempt));
            Files.deleteIfExists(attemptRerunFile);
            FailedScenarioExecutor.execute(failed, ATTEMPTS.resolve(String.format("attempt-%02d.json", attempt)),
                    attemptRerunFile);
            if (Files.exists(attemptRerunFile)) {
                Files.copy(attemptRerunFile, FAILED_SCENARIOS, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        int merged = CucumberJsonMerger.mergeAttempts(ATTEMPTS, CONSOLIDATED_JSON);
        System.out.printf("%n===== Consolidated %d run result(s) into %s =====%n", merged, CONSOLIDATED_JSON);

        List<String> stillFailing = RerunFile.readLines(FAILED_SCENARIOS);
        assertTrue(stillFailing.isEmpty(),
                () -> "Scenarios still failing after " + maxRetries + " retries:\n" + String.join("\n", stillFailing));
    }

    private static int nextAttemptNumber() throws IOException {
        if (!Files.isDirectory(ATTEMPTS)) {
            return 1;
        }
        try (Stream<Path> files = Files.list(ATTEMPTS)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(name -> name.matches("attempt-\\d+\\.json"))
                    .mapToInt(name -> Integer.parseInt(name.replaceAll("\\D", "")))
                    .max().orElse(0) + 1;
        }
    }
}
