package com.deck.server.repositories;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public class GameRepository
{
    private final JdbcClient db;
    public GameRepository(JdbcClient db) { this.db = db; }

    public UUID createGame()
    {
        UUID id = UUID.randomUUID();
        db.sql("INSERT INTO game(id) VALUES (:id)")
                .param("id", id).update();
        return id;
    }

    public void deleteGame(UUID gameId)
    {
        db.sql("DELETE FROM game WHERE id=:id").param("id", gameId).update();
    }

    public boolean doesGameExist(UUID gameId)
    {
        return db.sql("SELECT 1 FROM game WHERE id=:id")
                .param("id", gameId).query(Integer.class).optional().isPresent();
    }
}
