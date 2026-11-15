package org.test;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Runs the Cucumber scenarios in parallel. Parallelism is configured in {@code junit-platform.properties}
 * ({@code -Dthreads=N} overrides it) and the default tag expression is {@code cucumber.filter.tags}
 * there; pass {@code -Dcucumber.filter.tags=...} to select other scenarios.
 * Failed scenarios are written to {@code target/rerun/failed.txt} for {@link FailedTestRunner}.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.test.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "summary,"
        + "html:target/cucumber-report.html,"
        + "json:" + TestRunner.FIRST_RUN_JSON + ","
        + "rerun:" + TestRunner.FAILED_SCENARIOS_FILE + ","
        + "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:")
public class TestRunner {
    public static final String ATTEMPTS_DIRECTORY = "target/cucumber/attempts";
    public static final String FIRST_RUN_JSON = ATTEMPTS_DIRECTORY + "/attempt-00.json";
    public static final String FAILED_SCENARIOS_FILE = "target/rerun/failed.txt";
}
