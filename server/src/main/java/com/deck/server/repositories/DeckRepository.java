package com.deck.server.repositories;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DeckRepository
{
    private final JdbcClient db;

    public DeckRepository(JdbcClient db)
    {
        this.db = db;
    }

    public UUID createDeck(String name)
    {
        UUID id = UUID.randomUUID();
        db.sql("INSERT INTO deck(id, name) VALUES (:id, :name)")
                .param("id", id)
                .param("name", name)
                .update();
        return id;
    }

    public Optional<Map<String, Object>> getDeckById(UUID deckId)
    {
        return db.sql("SELECT id, name, created_at FROM deck WHERE id=:id")
                .param("id", deckId)
                .query()
                .listOfRows()
                .stream()
                .findFirst();
    }

    public List<Map<String, Object>> getAllDecks()
    {
        return db.sql("SELECT id, name, created_at FROM deck ORDER BY created_at")
                .query()
                .listOfRows();
    }

    public void renameDeck(UUID deckId, String newName)
    {
        db.sql("UPDATE deck SET name=:name WHERE id=:id")
                .param("id", deckId)
                .param("name", newName)
                .update();
    }

    public void deleteDeck(UUID deckId)
    {
        db.sql("DELETE FROM deck WHERE id=:id")
                .param("id", deckId)
                .update();
    }
}
