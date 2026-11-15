# Automation Test Framework

This repository contains API and browser UI tests written in Java. It uses
Cucumber for readable scenarios, JUnit 4 to run Cucumber, Spring to wire shared
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
    │   ├── TestRunner.java          # Cucumber/JUnit entry point
    │   ├── steps/                   # Cucumber step definitions
    │   ├── pages/                   # Selenium page objects
    │   ├── model/                   # API request/response data models
    │   └── support/                 # API, browser, URL and credential helpers
    └── resources/
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

`TestRunner` finds features on the test classpath at `features` and starts the
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

It is ignored by Git. Add profile-specific property keys there, for example:

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

Never commit real credentials. The credential file is local-only; use your
CI platform's protected secret store in automation pipelines.

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
reports.

```sh
# Run scenarios matching the default @uiSanity tag
mvn test

# Run a particular tag
mvn test -Dcucumber.filter.tags="@healthcheck"

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

The default tag expression is configured in
`src/test/java/org/test/TestRunner.java`; `-Dcucumber.filter.tags` selects or
overrides the scenarios for an individual run. Tags can be placed above a
feature or scenario. A scenario inherits its feature's tags.

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

Create GitHub Environments named `dev`, `pre`, and `prod` under
**Settings > Environments**. Add the values needed for each environment as
environment secrets; the workflow maps these secrets into the test process:

| Secret | Needed for |
| --- | --- |
| `SHOP_EMAIL` | Shop login UI scenarios |
| `SHOP_PASSWORD` | Shop login UI scenarios |
| `API_BASE_URL` | API tests when the selected profile has no configured URL |
| `API_KEY` | API tests when the selected profile has no configured key |
| `UI_BASE_URL` | UI scenarios when the selected profile has no configured UI URL |

For `pre` and `prod`, configure the API URL and API key before running API
scenarios; their checked-in URL settings are blank. Configure a UI URL and shop
credentials when running UI scenarios in those environments. The `dev`
environment uses the repository's test-service URL defaults, but UI login still
requires valid `SHOP_EMAIL` and `SHOP_PASSWORD` values.

The workflow uploads the Cucumber HTML/JSON, Extent, and Masterthought outputs
as a downloadable artifact on both successful and failed test runs.

## Reports

Reports are written under `target/`:

- `cucumber-report.html` — Cucumber HTML report
- `cucumber/cucumber.json` — Cucumber JSON output
- `extent/extent-report.html` — Extent Spark report
- `cucumber-html-reports/overview-features.html` — Masterthought HTML report

The Cucumber HTML, JSON, and Extent outputs are configured by `TestRunner` and
`src/extent.properties`. The Masterthought report goal is bound to Maven's
`test` phase in `pom.xml`, so it is generated when you run `mvn test`.
`mvn clean test` removes previous reports before running.
