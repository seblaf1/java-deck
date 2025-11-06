package com.deck.server.repositories;

import com.deck.server.entity.DeckCardEntity;
import com.deck.server.entity.DeckEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DeckRepository implements IDeckRepository
{
    private final JdbcClient db;

    public DeckRepository(JdbcClient db)
    {
        this.db = db;
    }

    @Override
    public UUID createDeck(String name)
    {
        UUID id = UUID.randomUUID();
        db.sql("INSERT INTO deck(id, name) VALUES (:id, :name)")
                .param("id", id)
                .param("name", name)
                .update();
        return id;
    }

    @Override
    public Optional<DeckEntity> getDeckById(UUID deckId)
    {
        return db.sql("SELECT id, name, created_at FROM deck WHERE id = :id")
                .param("id", deckId)
                .query((rs, rowNum) -> new DeckEntity(
                        (UUID) rs.getObject("id"),
                        rs.getString("name"),
                        rs.getObject("created_at", java.time.OffsetDateTime.class)
                ))
                .optional();
    }

    @Override
    public List<DeckEntity> getAllDecks()
    {
        return db.sql("SELECT id, name, created_at FROM deck ORDER BY created_at")
                .query((rs, rowNum) -> new DeckEntity(
                        (UUID) rs.getObject("id"),
                        rs.getString("name"),
                        rs.getObject("created_at", java.time.OffsetDateTime.class)
                ))
                .list();
    }

    @Override
    public void deleteDeck(UUID deckId)
    {
        db.sql("DELETE FROM deck WHERE id = :id")
                .param("id", deckId)
                .update();
    }

    @Override
    public boolean isDeckInUse(UUID deckId)
    {
        int count = db.sql("""
        SELECT COUNT(*)
        FROM deck_card dc
        WHERE dc.deck_id = :deckId
          AND (
              EXISTS (SELECT 1 FROM hand_card h WHERE h.card_id = dc.id)
              OR EXISTS (SELECT 1 FROM shoe_card s WHERE s.card_id = dc.id)
          )
    """)
                .param("deckId", deckId)
                .query(Integer.class)
                .optional()
                .orElse(0);

        return count > 0;
    }

    @Override
    public boolean doesDeckExist(UUID deckId)
    {
        int count = db.sql("""
        SELECT COUNT(*)
        FROM deck
        WHERE id = :id
        """)
                .param("id", deckId)
                .query(Integer.class)
                .single();

        return count > 0;
    }

    @Override
    public UUID addCardToDeck(UUID deckId, short cardDefId)
    {
        UUID id = UUID.randomUUID();
        db.sql("""
            
                        INSERT INTO deck_card(id, deck_id, card_def_id)
            VALUES (:id, :deckId, :cardDefId)
            """)
                .param("id", id)
                .param("deckId", deckId)
                .param("cardDefId", cardDefId)
                .update();
        return id;
    }

    @Override
    public List<DeckCardEntity> getCardsInDeck(UUID deckId)
    {
        return db.sql("""
            SELECT dc.id, dc.deck_id, dc.card_def_id
            FROM deck_card dc
            WHERE dc.deck_id = :deckId
            """)
                .param("deckId", deckId)
                .query((rs, rowNum) -> new DeckCardEntity(
                        (UUID) rs.getObject("id"),
                        (UUID) rs.getObject("deck_id"),
                        ((Number) rs.getObject("card_def_id")).shortValue()
                ))
                .list();
    }

    @Override
    public void removeCardFromDeck(UUID deckCardId)
    {
        db.sql("DELETE FROM deck_card WHERE id=:id")
                .param("id", deckCardId)
                .update();
    }

    @Override
    public void clearAllCardsFromDeck(UUID deckId)
    {
        db.sql("DELETE FROM deck_card WHERE deck_id=:deckId")
                .param("deckId", deckId)
                .update();
    }

    @Override
    public int countCardsInDeck(UUID deckId)
    {
        return db.sql("SELECT COUNT(*) FROM deck_card WHERE deck_id=:deckId")
                .param("deckId", deckId)
                .query(Integer.class)
                .single();
    }
}
