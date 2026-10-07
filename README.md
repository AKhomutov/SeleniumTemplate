# Selenium Automation Template

Reusable Java automation template for UI, API, and database-backed tests.

## Stack:

- Java 25
- Selenium 4
- TestNG
- Gradle
- AssertJ
- RestAssured
- HikariCP / JDBC
- Allure
- docker-selenium

## What is included:

### UI

- LOCAL and REMOTE WebDriver execution
- parallel-safe browser lifecycle
- explicit waits and stale-safe UI actions
- alerts and multiple-window handling
- Page Objects without `PageFactory` or `BasePage`
- deterministic local browser fixtures for framework tests

### API

A thin RestAssured-based client with:

- typed configuration
- fresh request state per call
- request customization for auth/headers
- request/response diagnostics
- no implicit status-code assertions

### Database

Lightweight JDBC infrastructure for test fixtures and DB checks:

- HikariCP connection pool
- prepared statements
- query/update/batch helpers
- generated keys
- explicit transactions
- no ORM or repository framework

### Reporting

Allure integration with failure diagnostics for:

- screenshots
- page source and current URL
- browser/session metadata
- API request/response
- SQL error context

Diagnostics are collected centrally rather than from Page Objects or test code.

## Run:

The default suite is deterministic and does not require Docker or an external application:

```bash
./gradlew test
```

The demo tests against [The Internet](https://the-internet.herokuapp.com/) are kept separately:

```bash
./gradlew testExternal
```

Generate an Allure report:

```bash
./gradlew allureReport
```

or open it directly:

```bash
./gradlew allureServe
```

## Configuration:

Configuration is loaded from HOCON files and converted to immutable typed records before reaching runtime code.

Available profiles:

- `local` — browser runs on the test machine
- `remote` — browser runs through a Selenium-compatible remote endpoint

Examples:

```bash
./gradlew testExternal -Dtest.profile=local
./gradlew testExternal -Dtest.profile=remote
```

Configuration precedence:

```text
JVM properties
environment variables
profile config
application.conf
```

See:

- `src/test/resources/application.conf`
- `src/test/resources/application-local.conf`
- `src/test/resources/application-remote.conf`

for the available settings.

## Remote browser:

The reference REMOTE setup uses official `docker-selenium`.

The image is pinned by both version and digest for reproducible runs.

Start Selenium:

```bash
docker compose -f docker-compose.selenium.yml up -d
./scripts/wait-for-selenium.sh
```

Run the deterministic remote smoke test:

```bash
./gradlew testRemoteSmoke -Dtest.profile=remote
```

Run the external demo suite:

```bash
./gradlew testExternal -Dtest.profile=remote
```

The browser can be watched live through noVNC at `http://localhost:7900`.

Default password: `secret`.

Stop the container:

```bash
docker compose -f docker-compose.selenium.yml down -v
```

The Java framework itself has no Docker-specific behavior. REMOTE execution is just a standard `RemoteWebDriver` endpoint, so the same code can be used with Selenium Grid, Selenoid, Moon, or cloud providers.

## Project structure:

- `core/browser` — WebDriver lifecycle and creation
- `core/ui` — actions, waits, alerts, and windows
- `core/config` — typed configuration
- `core/api` — RestAssured infrastructure
- `core/db` — JDBC infrastructure
- `demo/theinternet` — Page Objects, settings, and demo flows (assertions/tests live under `src/test`)
- `support` — test lifecycle and reporting

`ApplicationManager` owns browser-related services only. API and database infrastructure are composed separately.