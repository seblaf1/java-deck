package com.deck.server.repositories;

import com.deck.server.entity.PlayerEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PlayerRepository
{
    private final JdbcClient db;

    public PlayerRepository(JdbcClient db)
    {
        this.db = db;
    }

    public UUID addPlayerToGame(UUID gameId, UUID userId)
    {
        UUID id = UUID.randomUUID();
        db.sql("""
            INSERT INTO player(id, game_id, user_id)
            VALUES (:id, :gameId, :userId)
            ON CONFLICT (game_id, user_id) DO NOTHING
            """)
                .param("id", id)
                .param("gameId", gameId)
                .param("userId", userId)
                .update();
        return id;
    }

    public void removePlayerFromGame(UUID playerId)
    {
        db.sql("DELETE FROM player WHERE id = :id")
                .param("id", playerId)
                .update();
    }

    public List<PlayerEntity> getAllPlayersInGame(UUID gameId)
    {
        return db.sql("""
            SELECT p.id, p.game_id, p.user_id, u.name AS user_name, p.added_at
            FROM player p
            JOIN app_user u ON u.id = p.user_id
            WHERE p.game_id = :gameId
            ORDER BY p.added_at
            """)
                .param("gameId", gameId)
                .query((rs, rowNum) -> new PlayerEntity(
                        (UUID) rs.getObject("id"),
                        (UUID) rs.getObject("game_id"),
                        (UUID) rs.getObject("user_id"),
                        rs.getString("user_name"),
                        rs.getObject("added_at", java.time.OffsetDateTime.class)
                ))
                .list();
    }

    public boolean doesPlayerExist(UUID playerId)
    {
        return db.sql("SELECT 1 FROM player WHERE id = :id")
                .param("id", playerId)
                .query(Integer.class)
                .optional()
                .isPresent();
    }
}
