package com.example.seleniumtemplate.core.db;

import com.example.seleniumtemplate.core.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatabaseManager implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private final HikariDataSource dataSource;
    private final DatabaseClient client;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DatabaseManager(DatabaseConfig config) {
        Objects.requireNonNull(config, "config must not be null");

        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("selenium-template-db");
        hikari.setJdbcUrl(config.jdbcUrl());
        hikari.setUsername(config.username());
        hikari.setPassword(config.password());
        hikari.setMaximumPoolSize(config.maximumPoolSize());
        hikari.setConnectionTimeout(config.connectionTimeout().toMillis());

        this.dataSource = new HikariDataSource(hikari);
        this.client = new DatabaseClient(dataSource);
        log.info("Database pool started: maximumPoolSize={}", config.maximumPoolSize());
    }

    public DatabaseClient client() {
        return client;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            dataSource.close();
            log.info("Database pool closed");
        }
    }
}
