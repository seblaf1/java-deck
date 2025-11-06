package com.deck.server.repositories;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class UserRepository
{
    private final JdbcClient db;
    public UserRepository(JdbcClient db) { this.db = db; }

    public UUID createUser(String name)
    {
        UUID id = UUID.randomUUID();
        db.sql("INSERT INTO app_user(id, name) VALUES (:id, :name)")
                .param("id", id)
                .param("name", name)
                .update();
        return id;
    }

    public Optional<UUID> getUserIdByName(String name)
    {
        return db.sql("SELECT id FROM app_user WHERE name=:name")
                .param("name", name)
                .query(UUID.class)
                .optional();
    }

    public boolean doesUserExist(UUID userId)
    {
        return db.sql("SELECT 1 FROM app_user WHERE id=:id")
                .param("id", userId)
                .query(Integer.class)
                .optional()
                .isPresent();
    }

    public void deleteUser(UUID userId)
    {
        db.sql("DELETE FROM app_user WHERE id=:id")
                .param("id", userId)
                .update();
    }

    public List<Map<String, Object>> getAllUsers()
    {
        return db.sql("SELECT id, name, created_at FROM app_user ORDER BY created_at")
                .query()
                .listOfRows();
    }
}
