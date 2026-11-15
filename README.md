# Automation Test Framework

This repository contains API and browser UI tests written in Java. It uses
Cucumber for readable scenarios, the JUnit Platform to run Cucumber in parallel, Spring to wire shared
test components, Rest Assured for API requests, and Selenium WebDriver for
browser automation.

## Requirements

- Java 17
- Maven 3.6.3 or later
- A supported browser for UI tests: Chrome, Edge, or Safari
- Network access to the configured API and UI endpoints

The browser configured by default is Edge. Selenium Manager can manage Chrome
and Edge drivers; Safari requires macOS and Safari's **Allow Remote Automation**
setting. Safari does not support headless mode.

## Project layout

```text
src/
├── bootstrap.properties             # Default environment and browser
├── environment.properties           # API and UI base URLs by environment
├── extent.properties                # Extent report output settings
├── main/
│   ├── java/org/test/
│   │   ├── AutomationApplication.java
│   │   └── config/ApiProperties.java
│   └── resources/
│       ├── application.yml          # Common API endpoints/settings
│       └── application-{dev,pre,prod}.yml
└── test/
    ├── java/org/test/
    │   ├── TestRunner.java          # Parallel Cucumber/JUnit Platform suite (first run)
    │   ├── FailedTestRunner.java    # Re-runs failed scenarios and merges all results
    │   ├── reporting/               # Retry policy, rerun-file reader, JSON merger
    │   ├── steps/                   # Cucumber step definitions and hooks
    │   ├── pages/                   # Selenium page objects
    │   ├── model/                   # API request/response data models
    │   └── support/                 # API, browser, URL and credential helpers
    └── resources/
        ├── junit-platform.properties # Parallelism and default tag expression
        ├── features/                # Gherkin feature files
        ├── Payload/                 # JSON request templates for API tests
        └── LoginData/               # Local UI credentials (git-ignored)
```

### Where to add tests and shared code

- **Feature files:** add `.feature` files under
  `src/test/resources/features/`. Use Gherkin scenarios and tags to describe
  test behavior.
- **Step definitions:** add Java glue classes under
  `src/test/java/org/test/steps/`. The runner's `glue` setting is
  `org.test.steps`.
- **Utilities and shared support:** put reusable test helpers under
  `src/test/java/org/test/support/`. Examples include `PlaceApiSupport`,
  `BrowserFactory`, `EnvironmentUrlResolver`, and `ShoppingAppCredentials`.
- **Selenium page objects:** put browser page interactions under
  `src/test/java/org/test/pages/`; keep selectors and page actions out of step
  definitions where practical.
- **API models:** put request/response POJOs under
  `src/test/java/org/test/model/`.

`TestRunner` is a JUnit Platform `@Suite`. It finds features on the test classpath at `features` and starts the
Spring test context through `CucumberSpringConfiguration`.

## API request payloads

Keep JSON request templates in `src/test/resources/Payload/`. The Add Place
scenarios currently use `Payload/AddPlace.json`. `PlaceApiSupport` loads this
file from the test classpath and Jackson maps it to the `Place` model; scenario
Examples provide the variable values such as coordinates, name, address, and
types. Separate multiple types in an Examples cell with semicolons, for
example `shoe park;shop`.

For another API request, add its template in this directory and load it through
a shared support helper rather than embedding large JSON strings in step
definitions. Do not put credentials, API keys, or other secrets in payload
files or feature Examples.

## Credentials and secrets

### Shop UI credentials

The optional local credentials file is:

```text
src/test/resources/LoginData/ShoppingApp.properties
```

It is ignored by Git. Add profile-specific property keys there:

```properties
email_dev=your-dev-account-email
password_dev=your-dev-password
email_pre=your-pre-account-email
password_pre=your-pre-password
email_prod=your-prod-account-email
password_prod=your-prod-password
```

The framework selects the suffix matching the active Spring profile. The
`SHOP_EMAIL` and `SHOP_PASSWORD` environment variables override the file values
and are recommended for CI secret stores:

```sh
export SHOP_EMAIL='your-account-email'
read -rs SHOP_PASSWORD
export SHOP_PASSWORD
mvn test -Dcucumber.filter.tags="@login"
```

Keep actual credentials in the ignored local file or a CI secret store; do not
commit them. For GitHub Actions, configure `SHOP_EMAIL` and `SHOP_PASSWORD` as
secrets in the selected GitHub Environment (`dev`, `pre`, or `prod`).

### API key

The API key is configured by the selected Spring profile. The development
profile has a test-service default; pre-production and production require
`API_KEY`. Set secrets as environment variables rather than committing them:

```sh
API_KEY='your-api-key' mvn test -Dspring.profiles.active=pre
```

The API and UI base URLs can be overridden with `API_BASE_URL` and
`UI_BASE_URL`. Configure the environment-specific API/UI URLs in
`src/environment.properties` when setting up a new environment. The `pre` and
`prod` URLs in the checked-in file are blank until configured.

## Environment and browser configuration

`src/bootstrap.properties` sets the defaults:

```properties
environment=dev
browser=edge
```

The environment selects the Spring profile and the corresponding
`<environment>.api.url` / `<environment>.ui.url` entries in
`src/environment.properties`. Override the environment on the Maven command
line, for example:

```sh
mvn test -Dspring.profiles.active=pre
```

Common API endpoint paths and the default update address are in
`src/main/resources/application.yml`; profile-specific API URL and key
settings are in the matching `application-*.yml` files.

Choose a browser with `-Dbrowser=chrome`, `-Dbrowser=edge`, or
`-Dbrowser=safari`. Chrome and Edge support headless mode with
`-Dbrowser.headless=true`; omit it to see the browser.

## Running tests

Run commands from the repository root. Maven's `test` phase compiles the
project, runs the selected Cucumber scenarios, and generates the configured
reports. Prefer `mvn clean test` after changing branches or when Maven reports
`Nothing to compile` alongside unresolved-class errors; `clean` removes stale
compiled files under `target/`. The GitHub Actions workflow always uses a clean
build.

```sh
# Run scenarios matching the default @uiSanity tag
mvn test

# Run a particular tag
mvn clean test -Dcucumber.filter.tags="@healthcheck"

# Run API positive CRUD scenarios
mvn test -Dcucumber.filter.tags="@Sanity"

# Run the API regression scenarios, including negative and edge cases
mvn test -Dcucumber.filter.tags="@Regression"

# Run the shop login UI scenario
mvn test -Dcucumber.filter.tags="@login" -Dbrowser=chrome

# Run multiple tags using a Cucumber tag expression
mvn test -Dcucumber.filter.tags="@apiSanity or @uiSanity"

# Rebuild from a clean target directory
mvn clean test -Dcucumber.filter.tags="@healthcheck"
```

The default tag expression (`cucumber.filter.tags`) is configured in
`src/test/resources/junit-platform.properties`; `-Dcucumber.filter.tags` selects
or overrides the scenarios for an individual run. Tags can be placed above a
feature or scenario. A scenario inherits its feature's tags.

### Parallel execution

Scenarios run in parallel using the Cucumber JUnit Platform engine, configured
in `junit-platform.properties` and surefire in `pom.xml`. Each scenario runs on
one thread with its own browser and Spring-wired step objects. The default is 4
threads; change it with `-Dthreads`:

```sh
mvn clean test -Dthreads=2 -Dcucumber.filter.tags="@Regression"
```

Use `-Dthreads=1` for sequential execution (Safari supports only one session,
so the workflow always uses 1 thread for Safari).

### Re-running failed scenarios

`mvn test` runs three Surefire executions in order:

1. `default-test` - plain unit tests (a failure stops the build).
2. `cucumber-run` - `TestRunner` executes all selected scenarios in parallel and
   the Cucumber `rerun` plugin lists failed scenarios in `target/rerun/failed.txt`.
3. `rerun-failed` - `FailedTestRunner` executes only the scenarios still listed
   in `failed.txt`, then repeats with the ones that still fail, up to
   `-Dretry.count` times. The default is 3; the maximum is 4 (larger values are
   capped, `0` disables retries).

For example, if 100 scenarios run and 40 fail, those 40 are re-executed; if 20 of
them fail again, only those 20 run in the next retry, and so on until everything
passes or the retries are used up.

```sh
mvn clean test -Dretry.count=4
```

To run `FailedTestRunner` on its own against the results of an earlier run (after
`mvn test`), then rebuild the consolidated report:

```sh
mvn test-compile surefire:test@rerun-failed -Dretry.count=2
mvn net.masterthought:maven-cucumber-reporting:5.10.2:generate@cucumber-report
```

When only unit tests are selected (for example `-Dtest=RetryPolicyTest`), add
`-Dcucumber.skip=true` so the report goal is skipped.

These are integration tests: API scenarios send real HTTP requests to the
configured service. Add/Get/Update/Delete scenarios create and modify data and
attempt cleanup. Use a test environment and only run against systems for which
you have authorization; do not point destructive test scenarios at production
without explicit approval.

## Running tests in GitHub Actions

The manual workflow is `.github/workflows/tests.yml`. After it is pushed to the
repository's default branch, open **Actions > Automation tests > Run workflow**.
Use the **Use workflow from** selector to choose the branch to test. The
workflow checks out that selected branch and provides inputs for:

- **Browser:** Chrome, Edge, or Safari. Chrome and Edge run headlessly on
  Ubuntu; Safari runs on macOS because Safari is not available on Ubuntu.
- **Environment:** `dev`, `pre`, or `prod`, which selects the corresponding
  Spring profile and GitHub Environment.
- **Tags:** a Cucumber tag expression; the default is `@uiSanity`.
- **Retry count:** times to re-run failed scenarios (default 3, maximum 4).
- **Threads:** scenarios to run in parallel (default 4; Safari always uses 1).

You can add shared values as repository secrets under
**Settings > Secrets and variables > Actions**, or create GitHub Environments
named `dev`, `pre`, and `prod` under **Settings > Environments** and add
environment-specific secrets there. The workflow reads secrets from either
scope; an environment secret with the same name takes precedence over a
repository secret. The `SHOP_EMAIL` and `SHOP_PASSWORD` repository secrets
shown in GitHub Settings are already available to the workflow; no YAML change
is needed for them.

| Secret | Needed for |
| --- | --- |
| `SHOP_EMAIL` | Shop login UI scenarios; use repository or environment scope |
| `SHOP_PASSWORD` | Shop login UI scenarios; use repository or environment scope |
| `API_BASE_URL` | API tests when the selected profile has no configured URL |
| `API_KEY` | API tests when the selected profile has no configured key |
| `UI_BASE_URL` | UI scenarios when the selected profile has no configured UI URL |

For `pre` and `prod`, configure the API URL and API key before running API
scenarios; their checked-in URL settings are blank. Configure a UI URL and shop
credentials when running UI scenarios in those environments. The `dev`
environment uses the repository's test-service URL defaults, but UI login still
requires valid `SHOP_EMAIL` and `SHOP_PASSWORD` values.

API request/response logs, scenario start/finish lines and retry progress appear
in the workflow's console log, and a summary of the consolidated results is added
to the run's job summary. The workflow uploads the Cucumber HTML/JSON, Extent,
and Masterthought outputs (including the single consolidated report) as a
downloadable artifact on both successful and failed test runs. UI screenshots are
attached to the scenarios in the report and saved under `target/screenshots/` for
the artifact.

The shop login needs `SHOP_EMAIL` and `SHOP_PASSWORD` from repository secrets
or the selected GitHub Environment. If either is missing, the login step fails
before opening a browser, so no screenshot can be captured for that failure.

## API request/response logging

Every Rest Assured call made through `PlaceApiSupport.request()` is logged by
`ApiLoggingFilter`: method and URL, request headers and body, response status,
headers and body, and the response time. JSON is pretty-printed. The text goes
to the console (so it appears in the GitHub Actions log, tagged with the
scenario name) and is attached to the scenario, so it is also visible in the
Cucumber reports. The `Authorization`, `Cookie`, `Set-Cookie` and `X-Api-Key`
headers and the `key`, `api_key`, `token` and `password` query parameters are
masked as `****`.

## UI screenshots

For UI scenarios (any scenario that opens a browser and registers it with
`ScenarioContext`), screenshots are saved to `target/screenshots/` and attached to
the Cucumber report when:

- a step fails (`Step failed`, attached to the failing step);
- an assertion made with `UiAssertions` fails (`Assertion failed - <message>`),
  taken before the error is thrown; use `UiAssertions` for assertions in UI steps;
- a scenario fails, and at the end of every scenario
  (`Scenario failed (FAILED)` or `End of scenario (PASSED)`), before the browser
  closes.

Screenshot problems are logged and never change the test result. API-only
scenarios have no browser and take no screenshots.

## Reports

Reports are written under `target/`:

- `cucumber-html-reports/overview-features.html` - **the consolidated
  Masterthought report**: one entry per scenario with its final result after all
  retries, including the screenshots and API call logs
- `cucumber/cucumber.json` - consolidated Cucumber JSON, the input of that report
- `cucumber/attempts/attempt-NN.json` - raw JSON of the first run (`00`) and each retry
- `rerun/failed.txt` - scenarios still failing after the last attempt
- `screenshots/` - saved screenshots
- `cucumber-report.html` and `extent/extent-report.html` - Cucumber HTML and
  Extent Spark reports of the *first run only* (before retries)

`FailedTestRunner` merges the attempt files into `cucumber/cucumber.json`; a
scenario that was re-executed is replaced by its latest result. The Masterthought
goal is bound to Maven's `test` phase, runs after both Cucumber executions, and
fails the build if any scenario is still failing after the last retry (the report
is generated first). `mvn clean test` removes previous reports before running,
and a plain `mvn test` clears the previous run's `cucumber/`, `rerun/`,
`screenshots/` and `cucumber-html-reports/` directories.
