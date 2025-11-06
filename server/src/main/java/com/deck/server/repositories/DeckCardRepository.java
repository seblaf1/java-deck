package com.deck.server.repositories;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class DeckCardRepository
{
    private final JdbcClient db;
    public DeckCardRepository(JdbcClient db) { this.db = db; }

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

    public List<Map<String, Object>> getCardsInDeck(UUID deckId)
    {
        return db.sql("""
            SELECT dc.id, dc.card_def_id, cd.suit, cd.rank
            FROM deck_card dc
            JOIN card_definition cd ON cd.id = dc.card_def_id
            WHERE deck_id = :deckId
            """)
                .param("deckId", deckId)
                .query()
                .listOfRows();
    }

    public void removeCardFromDeck(UUID deckCardId)
    {
        db.sql("DELETE FROM deck_card WHERE id=:id")
                .param("id", deckCardId)
                .update();
    }

    public void clearAllCardsFromDeck(UUID deckId)
    {
        db.sql("DELETE FROM deck_card WHERE deck_id=:deckId")
                .param("deckId", deckId)
                .update();
    }

    public int countCardsInDeck(UUID deckId)
    {
        return db.sql("SELECT COUNT(*) FROM deck_card WHERE deck_id=:deckId")
                .param("deckId", deckId)
                .query(Integer.class)
                .single();
    }
}
