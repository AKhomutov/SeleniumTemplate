package com.example.seleniumtemplate.core.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatabaseClient {

    private static final Logger log = LoggerFactory.getLogger(DatabaseClient.class);

    private final DataSource dataSource;
    private final @Nullable Connection boundConnection;

    DatabaseClient(DataSource dataSource) {
        this(dataSource, null);
    }

    private DatabaseClient(DataSource dataSource, @Nullable Connection boundConnection) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
        this.boundConnection = boundConnection;
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(DatabaseClient db) throws Exception;
    }

    public int update(String sql, Object... params) {
        Objects.requireNonNull(sql, "sql must not be null");
        Objects.requireNonNull(params, "params must not be null");
        long started = System.nanoTime();
        try {
            int updated = withConnection(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    bind(statement, params);
                    return statement.executeUpdate();
                }
            });
            log.debug("DB update completed in {} ms: {}", elapsedMillis(started), sql);
            return updated;
        } catch (SQLException ex) {
            throw wrap("update", sql, started, ex);
        }
    }

    public int[] batch(String sql, List<Object[]> parameterSets) {
        Objects.requireNonNull(sql, "sql must not be null");
        Objects.requireNonNull(parameterSets, "parameterSets must not be null");
        long started = System.nanoTime();
        try {
            int[] counts = withConnection(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (Object[] params : parameterSets) {
                        Objects.requireNonNull(params, "parameter set must not be null");
                        bind(statement, params);
                        statement.addBatch();
                    }
                    return statement.executeBatch();
                }
            });
            log.debug("DB batch completed in {} ms: {}", elapsedMillis(started), sql);
            return counts;
        } catch (SQLException ex) {
            throw wrap("batch", sql, started, ex);
        }
    }

    public List<Map<String, Object>> query(String sql, Object... params) {
        Objects.requireNonNull(sql, "sql must not be null");
        Objects.requireNonNull(params, "params must not be null");
        long started = System.nanoTime();
        try {
            List<Map<String, Object>> rows = withConnection(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    bind(statement, params);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        return readAll(resultSet);
                    }
                }
            });
            log.debug("DB query completed in {} ms: {}", elapsedMillis(started), sql);
            return rows;
        } catch (SQLException ex) {
            throw wrap("query", sql, started, ex);
        }
    }

    public Map<String, Object> queryOne(String sql, Object... params) {
        List<Map<String, Object>> rows = query(sql, params);
        if (rows.isEmpty()) {
            throw new IllegalStateException("Expected exactly 1 row, but found 0 for SQL: " + sql);
        }
        if (rows.size() > 1) {
            throw new IllegalStateException(
                    "Expected exactly 1 row, but found " + rows.size() + " for SQL: " + sql);
        }
        return rows.getFirst();
    }

    public Optional<Map<String, Object>> queryOptional(String sql, Object... params) {
        List<Map<String, Object>> rows = query(sql, params);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        if (rows.size() > 1) {
            throw new IllegalStateException(
                    "Expected at most 1 row, but found " + rows.size() + " for SQL: " + sql);
        }
        return Optional.of(rows.getFirst());
    }

    public <T> T queryScalar(String sql, Class<T> type, Object... params) {
        Objects.requireNonNull(type, "type must not be null");
        List<Map<String, Object>> rows = query(sql, params);
        if (rows.isEmpty()) {
            throw new IllegalStateException("Expected exactly 1 row, but found 0 for SQL: " + sql);
        }
        if (rows.size() > 1) {
            throw new IllegalStateException(
                    "Expected exactly 1 row, but found " + rows.size() + " for SQL: " + sql);
        }
        Map<String, Object> row = rows.getFirst();
        if (row.isEmpty()) {
            throw new IllegalStateException("Scalar query returned a row with no columns: " + sql);
        }
        Object value = row.values().iterator().next();
        return convertScalar(value, type, sql);
    }

    public long insertAndReturnGeneratedKey(String sql, Object... params) {
        Objects.requireNonNull(sql, "sql must not be null");
        Objects.requireNonNull(params, "params must not be null");
        long started = System.nanoTime();
        try {
            long key = withConnection(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
                    bind(statement, params);
                    int updated = statement.executeUpdate();
                    if (updated != 1) {
                        throw new IllegalStateException(
                                "Expected insert to affect 1 row, but was " + updated
                                        + " for SQL: " + sql);
                    }
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new IllegalStateException(
                                    "Insert did not return a generated key for SQL: " + sql);
                        }
                        long generated = keys.getLong(1);
                        if (keys.wasNull()) {
                            throw new IllegalStateException(
                                    "Generated key was null for SQL: " + sql);
                        }
                        return generated;
                    }
                }
            });
            log.debug("DB insert completed in {} ms: {}", elapsedMillis(started), sql);
            return key;
        } catch (SQLException ex) {
            throw wrap("insertAndReturnGeneratedKey", sql, started, ex);
        }
    }

    public <T> T inTransaction(TransactionCallback<T> callback) {
        Objects.requireNonNull(callback, "callback must not be null");
        if (boundConnection != null) {
            throw new IllegalStateException("Nested transactions are not supported");
        }

        Connection connection = null;
        boolean committed = false;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            DatabaseClient txClient = new DatabaseClient(dataSource, connection);
            T result = callback.execute(txClient);
            connection.commit();
            committed = true;
            return result;
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Transaction failed: " + ex.getMessage(), ex);
        } finally {
            if (connection != null) {
                if (!committed) {
                    rollbackQuietly(connection);
                }
                restoreAutoCommitAndClose(connection);
            }
        }
    }

    private <T> T withConnection(SqlFunction<T> work) throws SQLException {
        Connection bound = boundConnection;
        if (bound != null) {
            return work.apply(bound);
        }
        try (Connection connection = dataSource.getConnection()) {
            return work.apply(connection);
        }
    }

    private static void bind(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    private static List<Map<String, Object>> readAll(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                String label = metaData.getColumnLabel(i);
                row.put(label, resultSet.getObject(i));
            }
            // Map.copyOf rejects null values; SQL NULL must stay as Java null.
            rows.add(Collections.unmodifiableMap(row));
        }
        return List.copyOf(rows);
    }

    private static <T> T convertScalar(@Nullable Object value, Class<T> type, String sql) {
        if (value == null) {
            throw new IllegalStateException("Scalar value was null for SQL: " + sql);
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        if (value instanceof Number number) {
            if (type == Long.class) {
                return type.cast(number.longValue());
            }
            if (type == Integer.class) {
                return type.cast(number.intValue());
            }
            if (type == Short.class) {
                return type.cast(number.shortValue());
            }
            if (type == Byte.class) {
                return type.cast(number.byteValue());
            }
            if (type == Double.class) {
                return type.cast(number.doubleValue());
            }
            if (type == Float.class) {
                return type.cast(number.floatValue());
            }
        }
        if (type == String.class) {
            return type.cast(value.toString());
        }
        if (type == Boolean.class) {
            if (value instanceof Boolean bool) {
                return type.cast(bool);
            }
            if (value instanceof Number number) {
                return type.cast(number.intValue() != 0);
            }
        }
        throw new IllegalStateException(
                "Cannot convert scalar of type " + value.getClass().getName()
                        + " to " + type.getName() + " for SQL: " + sql);
    }

    private static IllegalStateException wrap(
            String operation,
            String sql,
            long startedNanos,
            SQLException ex) {
        long elapsed = elapsedMillis(startedNanos);
        DatabaseDiagnostics.record(new DatabaseDiagnostics.Failure(
                operation,
                sql,
                ex.getSQLState(),
                ex.getErrorCode(),
                elapsed));
        log.warn(
                "DB {} failed: sql={}, SQLState={}, errorCode={}, elapsedMs={}",
                operation,
                sql,
                ex.getSQLState(),
                ex.getErrorCode(),
                elapsed);
        return new IllegalStateException(
                "DB " + operation + " failed: sql=" + sql
                        + ", SQLState=" + ex.getSQLState()
                        + ", errorCode=" + ex.getErrorCode()
                        + ", message=" + ex.getMessage(),
                ex);
    }

    private static void rollbackQuietly(@Nullable Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException ex) {
            log.warn(
                    "Rollback failed: SQLState={}, errorCode={}",
                    ex.getSQLState(),
                    ex.getErrorCode());
        }
    }

    private static void restoreAutoCommitAndClose(Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ex) {
            log.warn(
                    "Failed to restore autoCommit: SQLState={}, errorCode={}",
                    ex.getSQLState(),
                    ex.getErrorCode());
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            log.warn(
                    "Failed to close connection: SQLState={}, errorCode={}",
                    ex.getSQLState(),
                    ex.getErrorCode());
        }
    }

    private static long elapsedMillis(long startedNanos) {
        return (System.nanoTime() - startedNanos) / 1_000_000L;
    }

    @FunctionalInterface
    private interface SqlFunction<T> {
        T apply(Connection connection) throws SQLException;
    }
}
