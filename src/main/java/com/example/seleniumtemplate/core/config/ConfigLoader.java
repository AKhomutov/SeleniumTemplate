package com.example.seleniumtemplate.core.config;

import com.example.seleniumtemplate.core.browser.BrowserOptions;
import com.example.seleniumtemplate.core.browser.BrowserType;
import com.example.seleniumtemplate.core.browser.ExecutionMode;
import com.example.seleniumtemplate.core.browser.WindowSize;
import com.example.seleniumtemplate.core.ui.WaitPolicy;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public final class ConfigLoader {

    private static final String DEFAULT_PROFILE = "local";
    private static final Set<String> KNOWN_PROFILES = Set.of("local", "remote");

    private static final String PROFILE_PROPERTY = "test.profile";
    private static final String PROFILE_ENV = "TEST_PROFILE";

    private ConfigLoader() {
    }

    public static TestConfig load() {
        Properties systemProperties = copyProperties(System.getProperties());
        Map<String, String> environment = Map.copyOf(System.getenv());
        String profile = resolveProfile(environment, systemProperties);
        return load(profile, environment, systemProperties);
    }

    public static TestConfig load(
            String profile,
            Map<String, String> environment,
            Properties systemProperties) {
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(systemProperties, "systemProperties must not be null");

        String normalizedProfile = profile.trim().toLowerCase(Locale.ROOT);
        requireKnownProfile(normalizedProfile);

        Config base = parseRequiredResource("application.conf");
        Config profileConfig = parseRequiredResource("application-" + normalizedProfile + ".conf");
        Config envOverrides = environmentOverrides(environment);
        Config systemOverrides = ConfigFactory.parseProperties(copyProperties(systemProperties));

        // Precedence: JVM system properties > env overrides > profile > application.conf
        Config resolved = systemOverrides
                .withFallback(envOverrides)
                .withFallback(profileConfig)
                .withFallback(base)
                .resolve();

        return toTestConfig(resolved);
    }

    static String resolveProfile(Map<String, String> environment, Properties systemProperties) {
        String fromSystem = trimToNull(systemProperties.getProperty(PROFILE_PROPERTY));
        if (fromSystem != null) {
            return fromSystem;
        }
        String fromEnv = trimToNull(environment.get(PROFILE_ENV));
        if (fromEnv != null) {
            return fromEnv;
        }
        return DEFAULT_PROFILE;
    }

    private static void requireKnownProfile(String profile) {
        if (!KNOWN_PROFILES.contains(profile)) {
            throw new IllegalArgumentException(
                    "Unknown test profile '" + profile + "'. Known profiles: "
                            + String.join(", ", KNOWN_PROFILES));
        }
    }

    private static Config parseRequiredResource(String resourceName) {
        Config config = ConfigFactory.parseResourcesAnySyntax(resourceName);
        if (config.isEmpty()) {
            throw new IllegalStateException("Missing required config resource: " + resourceName);
        }
        return config;
    }

    private static Config environmentOverrides(Map<String, String> environment) {
        Map<String, Object> overrides = new LinkedHashMap<>();
        putOverride(overrides, environment, "TEST_BROWSER_TYPE", "test.browser.type");
        putOverride(overrides, environment, "TEST_EXECUTION_MODE", "test.browser.execution");
        putOverride(overrides, environment, "TEST_HEADLESS", "test.browser.headless");
        putOverride(overrides, environment, "TEST_REMOTE_URL", "test.browser.remote-url");
        putOverride(overrides, environment, "TEST_WINDOW_WIDTH", "test.browser.window.width");
        putOverride(overrides, environment, "TEST_WINDOW_HEIGHT", "test.browser.window.height");
        putOverride(overrides, environment, "TEST_WAIT_TIMEOUT", "test.waits.timeout");
        putOverride(overrides, environment, "TEST_WAIT_POLLING", "test.waits.polling");
        putOverride(overrides, environment, "TEST_WEB_BASE_URL", "test.environment.web-base-url");
        putOverride(overrides, environment, "TEST_API_BASE_URL", "test.environment.api.base-url");
        putOverride(
                overrides,
                environment,
                "TEST_API_CONNECT_TIMEOUT",
                "test.environment.api.connect-timeout");
        putOverride(
                overrides,
                environment,
                "TEST_API_RESPONSE_TIMEOUT",
                "test.environment.api.response-timeout");
        putOverride(
                overrides,
                environment,
                "TEST_DB_JDBC_URL",
                "test.environment.database.jdbc-url");
        putOverride(
                overrides,
                environment,
                "TEST_DB_USERNAME",
                "test.environment.database.username");
        putOverride(
                overrides,
                environment,
                "TEST_DB_PASSWORD",
                "test.environment.database.password");
        putOverride(
                overrides,
                environment,
                "TEST_DB_MAXIMUM_POOL_SIZE",
                "test.environment.database.maximum-pool-size");
        putOverride(
                overrides,
                environment,
                "TEST_DB_CONNECTION_TIMEOUT",
                "test.environment.database.connection-timeout");
        if (overrides.isEmpty()) {
            return ConfigFactory.empty();
        }
        return ConfigFactory.parseMap(overrides, "environment overrides");
    }

    private static void putOverride(
            Map<String, Object> overrides,
            Map<String, String> environment,
            String envName,
            String configPath) {
        String value = trimToNull(environment.get(envName));
        if (value != null) {
            overrides.put(configPath, value);
        }
    }

    private static TestConfig toTestConfig(Config config) {
        try {
            BrowserOptions browser = toBrowserOptions(config);
            WaitPolicy waits = toWaitPolicy(config);
            EnvironmentConfig environment = toEnvironmentConfig(config);
            return new TestConfig(new FrameworkConfig(browser, waits), environment);
        } catch (ConfigException ex) {
            throw new IllegalArgumentException(
                    "Invalid test configuration: " + ex.getMessage(), ex);
        }
    }

    private static BrowserOptions toBrowserOptions(Config config) {
        BrowserType browserType = parseEnum(
                "test.browser.type",
                requireString(config, "test.browser.type"),
                BrowserType.class);
        ExecutionMode executionMode = parseEnum(
                "test.browser.execution",
                requireString(config, "test.browser.execution"),
                ExecutionMode.class);
        boolean headless = requireBoolean(config, "test.browser.headless");
        int width = requirePositiveInt(config, "test.browser.window.width");
        int height = requirePositiveInt(config, "test.browser.window.height");
        WindowSize windowSize = new WindowSize(width, height);

        URI remoteUrl = null;
        if (executionMode == ExecutionMode.REMOTE) {
            remoteUrl = requireHttpRemoteUrl(config, "test.browser.remote-url");
        }

        return BrowserOptions.builder()
                .browser(browserType)
                .headless(headless)
                .executionMode(executionMode)
                .remoteUrl(remoteUrl)
                .windowSize(windowSize)
                .build();
    }

    private static WaitPolicy toWaitPolicy(Config config) {
        Duration timeout = requirePositiveDuration(config, "test.waits.timeout");
        Duration polling = requirePositiveDuration(config, "test.waits.polling");
        return new WaitPolicy(timeout, polling);
    }

    private static EnvironmentConfig toEnvironmentConfig(Config config) {
        URI webBaseUrl = requireAbsoluteUri(config, "test.environment.web-base-url");
        ApiConfig api = toApiConfig(config);
        DatabaseConfig database = toDatabaseConfig(config);
        return new EnvironmentConfig(webBaseUrl, api, database);
    }

    private static ApiConfig toApiConfig(Config config) {
        URI baseUrl = requireHttpUri(config, "test.environment.api.base-url");
        Duration connectTimeout =
                requirePositiveDuration(config, "test.environment.api.connect-timeout");
        Duration responseTimeout =
                requirePositiveDuration(config, "test.environment.api.response-timeout");
        return new ApiConfig(baseUrl, connectTimeout, responseTimeout);
    }

    private static DatabaseConfig toDatabaseConfig(Config config) {
        String jdbcUrl = requireString(config, "test.environment.database.jdbc-url");
        String username = requirePresentString(config, "test.environment.database.username");
        String password = requirePresentString(config, "test.environment.database.password");
        int maximumPoolSize =
                requirePositiveInt(config, "test.environment.database.maximum-pool-size");
        Duration connectionTimeout =
                requirePositiveDuration(config, "test.environment.database.connection-timeout");
        return new DatabaseConfig(jdbcUrl, username, password, maximumPoolSize, connectionTimeout);
    }

    private static URI requireHttpRemoteUrl(Config config, String path) {
        if (!config.hasPath(path) || config.getIsNull(path)) {
            throw new IllegalArgumentException(
                    path + " is required when test.browser.execution=REMOTE");
        }
        String raw = trimToNull(config.getString(path));
        if (raw == null) {
            throw new IllegalArgumentException(
                    path + " is required when test.browser.execution=REMOTE, but was blank");
        }
        return requireHttpUriValue(path, raw);
    }

    private static URI requireHttpUri(Config config, String path) {
        String raw = requireString(config, path);
        return requireHttpUriValue(path, raw);
    }

    private static URI requireHttpUriValue(String path, String raw) {
        URI uri = parseUri(path, raw);
        String scheme = uri.getScheme();
        if (scheme == null
                || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new IllegalArgumentException(
                    path + " must be an absolute http/https URI, but was: " + raw);
        }
        if (uri.getHost() == null) {
            throw new IllegalArgumentException(
                    path + " must include a host, but was: " + raw);
        }
        return uri;
    }

    private static URI requireAbsoluteUri(Config config, String path) {
        String raw = requireString(config, path);
        URI uri = parseUri(path, raw);
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException(
                    path + " must be an absolute URI, but was: " + raw);
        }
        return uri;
    }

    private static URI parseUri(String path, String raw) {
        try {
            return new URI(raw);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(
                    path + " is not a valid URI: " + raw, ex);
        }
    }

    private static String requireString(Config config, String path) {
        if (!config.hasPath(path) || config.getIsNull(path)) {
            throw new IllegalArgumentException("Missing required config key: " + path);
        }
        String value = trimToNull(config.getString(path));
        if (value == null) {
            throw new IllegalArgumentException(path + " must not be blank");
        }
        return value;
    }

    private static String requirePresentString(Config config, String path) {
        if (!config.hasPath(path) || config.getIsNull(path)) {
            throw new IllegalArgumentException("Missing required config key: " + path);
        }
        return config.getString(path);
    }

    private static boolean requireBoolean(Config config, String path) {
        if (!config.hasPath(path) || config.getIsNull(path)) {
            throw new IllegalArgumentException("Missing required config key: " + path);
        }
        try {
            return config.getBoolean(path);
        } catch (ConfigException ex) {
            throw new IllegalArgumentException(
                    path + " must be a boolean, but was: " + config.getValue(path).unwrapped(),
                    ex);
        }
    }

    private static int requirePositiveInt(Config config, String path) {
        if (!config.hasPath(path) || config.getIsNull(path)) {
            throw new IllegalArgumentException("Missing required config key: " + path);
        }
        int value;
        try {
            value = config.getInt(path);
        } catch (ConfigException ex) {
            throw new IllegalArgumentException(
                    path + " must be an integer, but was: " + config.getValue(path).unwrapped(),
                    ex);
        }
        if (value <= 0) {
            throw new IllegalArgumentException(path + " must be > 0, but was: " + value);
        }
        return value;
    }

    private static Duration requirePositiveDuration(Config config, String path) {
        if (!config.hasPath(path) || config.getIsNull(path)) {
            throw new IllegalArgumentException("Missing required config key: " + path);
        }
        Duration value;
        try {
            value = config.getDuration(path);
        } catch (ConfigException ex) {
            throw new IllegalArgumentException(
                    path + " must be a positive duration, but was: "
                            + config.getValue(path).unwrapped(),
                    ex);
        }
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(path + " must be > 0, but was: " + value);
        }
        return value;
    }

    private static <E extends Enum<E>> E parseEnum(String path, String raw, Class<E> type) {
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    path + " has unsupported value '" + raw + "'. Supported: "
                            + String.join(", ", enumNames(type)),
                    ex);
        }
    }

    private static <E extends Enum<E>> String[] enumNames(Class<E> type) {
        E[] constants = Objects.requireNonNull(
                type.getEnumConstants(),
                "enum constants must not be null for " + type.getName());
        String[] names = new String[constants.length];
        for (int i = 0; i < constants.length; i++) {
            names[i] = constants[i].name();
        }
        return names;
    }

    private static Properties copyProperties(Properties source) {
        Properties copy = new Properties();
        copy.putAll(source);
        return copy;
    }

    private static @Nullable String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
