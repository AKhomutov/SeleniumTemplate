package com.example.seleniumtemplate.core.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.seleniumtemplate.core.browser.BrowserType;
import com.example.seleniumtemplate.core.browser.ExecutionMode;
import com.example.seleniumtemplate.core.browser.WindowSize;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.testng.annotations.Test;

public class ConfigLoaderTest {

    @Test
    public void defaultConfigLoadsWithLocalProfileSemantics() {
        TestConfig config = ConfigLoader.load("local", Map.of(), new Properties());
        WindowSize windowSize = Objects.requireNonNull(
                config.framework().browser().windowSize(),
                "windowSize");

        assertThat(config.framework().browser().browser()).isEqualTo(BrowserType.CHROME);
        assertThat(config.framework().browser().executionMode()).isEqualTo(ExecutionMode.LOCAL);
        assertThat(config.framework().browser().remoteUrl()).isNull();
        assertThat(windowSize.width()).isEqualTo(1280);
        assertThat(windowSize.height()).isEqualTo(800);
        assertThat(config.framework().waits().timeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(config.framework().waits().pollingInterval()).isEqualTo(Duration.ofMillis(100));
        assertThat(config.environment().webBaseUrl().toString())
                .isEqualTo("https://the-internet.herokuapp.com/");
        assertThat(config.environment().api().baseUrl().toString())
                .isEqualTo("http://127.0.0.1:8080");
        assertThat(config.environment().api().connectTimeout()).isEqualTo(Duration.ofSeconds(3));
        assertThat(config.environment().api().responseTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(config.environment().database().jdbcUrl())
                .startsWith("jdbc:h2:mem:selenium_template");
        assertThat(config.environment().database().username()).isEqualTo("sa");
        assertThat(config.environment().database().password()).isEmpty();
        assertThat(config.environment().database().maximumPoolSize()).isEqualTo(5);
        assertThat(config.environment().database().connectionTimeout())
                .isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    public void apiBaseUrlDefaultLoads() {
        TestConfig config = ConfigLoader.load("local", Map.of(), new Properties());

        assertThat(config.environment().api().baseUrl().toString())
                .isEqualTo("http://127.0.0.1:8080");
    }

    @Test
    public void databaseConfigDefaultLoadsSafeH2Values() {
        TestConfig config = ConfigLoader.load("local", Map.of(), new Properties());
        DatabaseConfig database = config.environment().database();

        assertThat(database.jdbcUrl()).contains("jdbc:h2:mem:");
        assertThat(database.username()).isEqualTo("sa");
        assertThat(database.password()).isEmpty();
        assertThat(database.maximumPoolSize()).isEqualTo(5);
        assertThat(database.connectionTimeout()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    public void databaseEnvironmentOverrideBeatsDefault() {
        TestConfig config = ConfigLoader.load(
                "local",
                Map.of(
                        "TEST_DB_JDBC_URL", "jdbc:h2:mem:from_env;DB_CLOSE_DELAY=-1",
                        "TEST_DB_USERNAME", "env_user",
                        "TEST_DB_PASSWORD", "env-secret",
                        "TEST_DB_MAXIMUM_POOL_SIZE", "7",
                        "TEST_DB_CONNECTION_TIMEOUT", "8s"),
                new Properties());

        assertThat(config.environment().database().jdbcUrl())
                .isEqualTo("jdbc:h2:mem:from_env;DB_CLOSE_DELAY=-1");
        assertThat(config.environment().database().username()).isEqualTo("env_user");
        assertThat(config.environment().database().password()).isEqualTo("env-secret");
        assertThat(config.environment().database().maximumPoolSize()).isEqualTo(7);
        assertThat(config.environment().database().connectionTimeout())
                .isEqualTo(Duration.ofSeconds(8));
    }

    @Test
    public void databaseSystemPropertyBeatsEnvironment() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty(
                "test.environment.database.jdbc-url",
                "jdbc:h2:mem:from_jvm;DB_CLOSE_DELAY=-1");
        systemProperties.setProperty("test.environment.database.username", "jvm_user");
        systemProperties.setProperty("test.environment.database.password", "jvm-secret");
        systemProperties.setProperty("test.environment.database.maximum-pool-size", "3");
        systemProperties.setProperty("test.environment.database.connection-timeout", "2s");

        TestConfig config = ConfigLoader.load(
                "local",
                Map.of(
                        "TEST_DB_JDBC_URL", "jdbc:h2:mem:from_env;DB_CLOSE_DELAY=-1",
                        "TEST_DB_USERNAME", "env_user",
                        "TEST_DB_PASSWORD", "env-secret",
                        "TEST_DB_MAXIMUM_POOL_SIZE", "7",
                        "TEST_DB_CONNECTION_TIMEOUT", "8s"),
                systemProperties);

        assertThat(config.environment().database().jdbcUrl())
                .isEqualTo("jdbc:h2:mem:from_jvm;DB_CLOSE_DELAY=-1");
        assertThat(config.environment().database().username()).isEqualTo("jvm_user");
        assertThat(config.environment().database().password()).isEqualTo("jvm-secret");
        assertThat(config.environment().database().maximumPoolSize()).isEqualTo(3);
        assertThat(config.environment().database().connectionTimeout())
                .isEqualTo(Duration.ofSeconds(2));
    }

    @Test
    public void invalidDatabasePoolSizeFailsClearly() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.environment.database.maximum-pool-size", "0");

        assertThatThrownBy(() -> ConfigLoader.load("local", Map.of(), systemProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test.environment.database.maximum-pool-size");
    }

    @Test
    public void invalidDatabaseConnectionTimeoutFailsClearly() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.environment.database.connection-timeout", "0s");

        assertThatThrownBy(() -> ConfigLoader.load("local", Map.of(), systemProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test.environment.database.connection-timeout");
    }

    @Test
    public void blankDatabaseJdbcUrlFailsClearly() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.environment.database.jdbc-url", "   ");

        assertThatThrownBy(() -> ConfigLoader.load("local", Map.of(), systemProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test.environment.database.jdbc-url")
                .hasMessageContaining("blank");
    }

    @Test
    public void databasePasswordIsNotExposedInToStringOrValidationErrors() {
        DatabaseConfig database = new DatabaseConfig(
                "jdbc:h2:mem:secret_check;DB_CLOSE_DELAY=-1",
                "sa",
                "super-secret-password",
                2,
                Duration.ofSeconds(3));

        assertThat(database.toString()).doesNotContain("super-secret-password");
        assertThat(database.toString()).contains("password=***");

        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.environment.database.jdbc-url", "   ");
        systemProperties.setProperty(
                "test.environment.database.password",
                "super-secret-password");

        assertThatThrownBy(() -> ConfigLoader.load("local", Map.of(), systemProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageNotContaining("super-secret-password");
    }

    @Test
    public void apiBaseUrlEnvironmentOverrideBeatsDefault() {
        TestConfig config = ConfigLoader.load(
                "local",
                Map.of("TEST_API_BASE_URL", "https://api.example.test"),
                new Properties());

        assertThat(config.environment().api().baseUrl().toString())
                .isEqualTo("https://api.example.test");
    }

    @Test
    public void apiBaseUrlSystemPropertyBeatsEnvironment() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.environment.api.base-url", "https://jvm.example.test");

        TestConfig config = ConfigLoader.load(
                "local",
                Map.of("TEST_API_BASE_URL", "https://env.example.test"),
                systemProperties);

        assertThat(config.environment().api().baseUrl().toString())
                .isEqualTo("https://jvm.example.test");
    }

    @Test
    public void invalidApiBaseUrlFailsClearly() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.environment.api.base-url", "ftp://localhost/api");

        assertThatThrownBy(() -> ConfigLoader.load("local", Map.of(), systemProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test.environment.api.base-url")
                .hasMessageContaining("http/https");
    }

    @Test
    public void invalidApiConnectTimeoutFailsClearly() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.environment.api.connect-timeout", "0s");

        assertThatThrownBy(() -> ConfigLoader.load("local", Map.of(), systemProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test.environment.api.connect-timeout");
    }

    @Test
    public void localProfileUsesLocalExecution() {
        TestConfig config = ConfigLoader.load("local", Map.of(), new Properties());

        assertThat(config.framework().browser().executionMode()).isEqualTo(ExecutionMode.LOCAL);
    }

    @Test
    public void remoteProfileUsesRemoteExecutionAndRemoteUrl() {
        TestConfig config = ConfigLoader.load("remote", Map.of(), new Properties());
        URI remoteUrl = Objects.requireNonNull(
                config.framework().browser().remoteUrl(),
                "remoteUrl");

        assertThat(config.framework().browser().executionMode()).isEqualTo(ExecutionMode.REMOTE);
        assertThat(remoteUrl.toString()).isEqualTo("http://localhost:4444");
        assertThat(config.framework().browser().headless()).isFalse();
    }

    @Test
    public void profileOverridesBaseExecutionMode() {
        TestConfig local = ConfigLoader.load("local", Map.of(), new Properties());
        TestConfig remote = ConfigLoader.load("remote", Map.of(), new Properties());

        assertThat(local.framework().browser().executionMode()).isEqualTo(ExecutionMode.LOCAL);
        assertThat(remote.framework().browser().executionMode()).isEqualTo(ExecutionMode.REMOTE);
    }

    @Test
    public void environmentOverrideBeatsProfile() {
        TestConfig config = ConfigLoader.load(
                "remote",
                Map.of("TEST_EXECUTION_MODE", "LOCAL"),
                new Properties());

        assertThat(config.framework().browser().executionMode()).isEqualTo(ExecutionMode.LOCAL);
        assertThat(config.framework().browser().remoteUrl()).isNull();
    }

    @Test
    public void systemPropertyOverrideBeatsEnvironment() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.browser.execution", "REMOTE");
        systemProperties.setProperty("test.browser.remote-url", "http://grid.example:4444");

        TestConfig config = ConfigLoader.load(
                "local",
                Map.of(
                        "TEST_EXECUTION_MODE", "LOCAL",
                        "TEST_REMOTE_URL", "http://env.example:4444"),
                systemProperties);
        URI remoteUrl = Objects.requireNonNull(
                config.framework().browser().remoteUrl(),
                "remoteUrl");

        assertThat(config.framework().browser().executionMode()).isEqualTo(ExecutionMode.REMOTE);
        assertThat(remoteUrl.toString()).isEqualTo("http://grid.example:4444");
    }

    @Test
    public void invalidBrowserTypeFailsWithConfigKey() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.browser.type", "SAFARI");

        assertThatThrownBy(() -> ConfigLoader.load("local", Map.of(), systemProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test.browser.type")
                .hasMessageContaining("SAFARI");
    }

    @Test
    public void invalidExecutionModeFailsWithConfigKey() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.browser.execution", "CLOUD");

        assertThatThrownBy(() -> ConfigLoader.load("local", Map.of(), systemProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test.browser.execution")
                .hasMessageContaining("CLOUD");
    }

    @Test
    public void invalidTimeoutFailsWithConfigKey() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.waits.timeout", "0s");

        assertThatThrownBy(() -> ConfigLoader.load("local", Map.of(), systemProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test.waits.timeout");
    }

    @Test
    public void remoteWithBlankRemoteUrlFailsClearly() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.browser.execution", "REMOTE");
        systemProperties.setProperty("test.browser.remote-url", "   ");

        assertThatThrownBy(() -> ConfigLoader.load("local", Map.of(), systemProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test.browser.remote-url")
                .hasMessageContaining("REMOTE");
    }

    @Test
    public void remoteWithNonHttpRemoteUrlFailsClearly() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.browser.execution", "REMOTE");
        systemProperties.setProperty("test.browser.remote-url", "ftp://localhost:21");

        assertThatThrownBy(() -> ConfigLoader.load("local", Map.of(), systemProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("test.browser.remote-url")
                .hasMessageContaining("http/https");
    }

    @Test
    public void unknownProfileFailsFast() {
        assertThatThrownBy(() -> ConfigLoader.load("staging", Map.of(), new Properties()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown test profile")
                .hasMessageContaining("staging");
    }

    @Test
    public void resolveProfilePrefersSystemPropertyOverEnvironment() {
        Properties systemProperties = new Properties();
        systemProperties.setProperty("test.profile", "remote");

        String profile = ConfigLoader.resolveProfile(
                Map.of("TEST_PROFILE", "local"),
                systemProperties);

        assertThat(profile).isEqualTo("remote");
    }

    @Test
    public void resolveProfileUsesEnvironmentWhenSystemPropertyAbsent() {
        String profile = ConfigLoader.resolveProfile(
                Map.of("TEST_PROFILE", "remote"),
                new Properties());

        assertThat(profile).isEqualTo("remote");
    }
}
