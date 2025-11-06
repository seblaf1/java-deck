package com.deck.server.repositories;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class CardDefinitionRepository
{
    private final JdbcClient db;
    public CardDefinitionRepository(JdbcClient db) { this.db = db; }

    public void populateAll()
    {
        // 52 cards (suit 0–3, rank 1–13)
        for (int suit = 0; suit < 4; suit++)
        {
            for (int rank = 1; rank <= 13; rank++)
            {
                db.sql("""
                    INSERT INTO card_definition(suit, rank)
                    VALUES (:suit, :rank)
                    ON CONFLICT (suit, rank) DO NOTHING
                    """)
                        .param("suit", suit)
                        .param("rank", rank)
                        .update();
            }
        }
    }

    public List<Map<String, Object>> getAll()
    {
        return db.sql("SELECT id, suit, rank FROM card_definition ORDER BY suit, rank")
                .query()
                .listOfRows();
    }

    public Optional<Map<String, Object>> getById(short id)
    {
        return db.sql("SELECT id, suit, rank FROM card_definition WHERE id=:id")
                .param("id", id)
                .query()
                .listOfRows()
                .stream()
                .findFirst();
    }
}
