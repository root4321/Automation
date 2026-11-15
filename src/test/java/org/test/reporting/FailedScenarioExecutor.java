package org.test.reporting;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.FilePosition;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/** Executes only the given failed scenarios (in parallel, as configured) with the same glue as {@code TestRunner}. */
public final class FailedScenarioExecutor {
    private FailedScenarioExecutor() {
    }

    /**
     * @param jsonReport   where this attempt's Cucumber JSON is written
     * @param newRerunFile where scenarios failing in this attempt are written
     */
    public static void execute(List<RerunFile.Entry> failed, Path jsonReport, Path newRerunFile) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectors(failed))
                .configurationParameter(GLUE_PROPERTY_NAME, "org.test.steps")
                .configurationParameter(PLUGIN_PROPERTY_NAME,
                        "summary,json:" + jsonReport + ",rerun:" + newRerunFile)
                .build();
        LauncherFactory.create().execute(request);
    }

    static List<DiscoverySelector> selectors(List<RerunFile.Entry> failed) {
        List<DiscoverySelector> selectors = new ArrayList<>();
        for (RerunFile.Entry entry : failed) {
            List<FilePosition> positions = entry.lines().isEmpty()
                    ? java.util.Collections.singletonList(null)
                    : entry.lines().stream().map(FilePosition::from).toList();
            for (FilePosition position : positions) {
                selectors.add(selector(entry.uri(), position));
            }
        }
        return selectors;
    }

    private static DiscoverySelector selector(String uri, FilePosition position) {
        if (uri.startsWith("classpath:")) {
            String resource = uri.substring("classpath:".length()).replaceFirst("^/+", "");
            return position == null ? DiscoverySelectors.selectClasspathResource(resource)
                    : DiscoverySelectors.selectClasspathResource(resource, position);
        }
        String file = uri.startsWith("file:") ? Path.of(URI.create(uri)).toString() : uri;
        return position == null ? DiscoverySelectors.selectFile(file) : DiscoverySelectors.selectFile(file, position);
    }
}
