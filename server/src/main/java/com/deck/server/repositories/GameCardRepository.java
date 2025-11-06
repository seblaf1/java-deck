package com.deck.server.repositories;

import com.deck.server.entity.GameCardEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class GameCardRepository
{
    private final JdbcClient db;

    public GameCardRepository(JdbcClient db)
    {
        this.db = db;
    }

    public void addCardToShoe(UUID gameId, UUID cardId, long orderKey)
    {
        db.sql("""
            INSERT INTO shoe_card (game_id, card_id, order_key)
            VALUES (:gameId, :cardId, :orderKey)
            ON CONFLICT DO NOTHING
            """)
                .param("gameId", gameId)
                .param("cardId", cardId)
                .param("orderKey", orderKey)
                .update();
    }

    public List<GameCardEntity> getShoeCards(UUID gameId)
    {
        return db.sql("""
            SELECT game_id, card_id, order_key
            FROM shoe_card
            WHERE game_id = :gameId
            ORDER BY order_key
            """)
                .param("gameId", gameId)
                .query((rs, rowNum) -> new GameCardEntity(
                        rs.getObject("game_id", UUID.class),
                        rs.getObject("card_id", UUID.class),
                        rs.getLong("order_key")
                ))
                .list();
    }

    public void clearShoe(UUID gameId)
    {
        db.sql("DELETE FROM shoe_card WHERE game_id = :gameId")
                .param("gameId", gameId)
                .update();
    }

    public int countRemainingCards(UUID gameId)
    {
        return db.sql("SELECT COUNT(*) FROM shoe_card WHERE game_id = :gameId")
                .param("gameId", gameId)
                .query(Integer.class)
                .single();
    }

    public long getNextIndexForGame(UUID gameId)
    {
        Long max = db.sql("SELECT MAX(order_key) FROM shoe_card WHERE game_id = :gameId")
                .param("gameId", gameId)
                .query(Long.class)
                .optional()
                .orElse(0L);
        return max + 1;
    }

    /// Swap two order_key values atomically
    public void swapCardsInPlace(UUID gameId, UUID cardA, long indexA, UUID cardB, long indexB)
    {
        // Step 1: temporarily move one record out of conflict range to respect constraints
        long tempKey = -1L * System.nanoTime(); // unique negative sentinel

        db.sql("""
            UPDATE shoe_card
            SET order_key = :tempKey
            WHERE game_id = :gameId AND card_id = :cardA
            """)
                .param("tempKey", tempKey)
                .param("gameId", gameId)
                .param("cardA", cardA)
                .update();

        db.sql("""
            UPDATE shoe_card
            SET order_key = :indexA
            WHERE game_id = :gameId AND card_id = :cardB
            """)
                .param("indexA", indexA)
                .param("gameId", gameId)
                .param("cardB", cardB)
                .update();

        db.sql("""
            UPDATE shoe_card
            SET order_key = :indexB
            WHERE game_id = :gameId AND card_id = :cardA
            """)
                .param("indexB", indexB)
                .param("gameId", gameId)
                .param("cardA", cardA)
                .update();
    }
}
