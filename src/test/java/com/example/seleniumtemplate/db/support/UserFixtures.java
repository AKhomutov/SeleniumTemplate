package com.example.seleniumtemplate.db.support;

import com.example.seleniumtemplate.core.db.DatabaseClient;
import java.util.Objects;

public final class UserFixtures {

    private final DatabaseClient db;

    public UserFixtures(DatabaseClient db) {
        this.db = Objects.requireNonNull(db, "db must not be null");
    }

    public long createUser(String email, String name) {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(name, "name must not be null");
        return db.insertAndReturnGeneratedKey(
                "INSERT INTO users(email, name, active) VALUES (?, ?, ?)",
                email,
                name,
                true);
    }

    public void deleteUser(long id) {
        db.update("DELETE FROM users WHERE id = ?", id);
    }
}
