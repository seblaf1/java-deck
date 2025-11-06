package com.deck.server.repositories;

import com.deck.server.entity.CardDefinition;
import com.deck.server.entity.Rank;
import com.deck.server.entity.Suit;
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
        for (Suit suit : Suit.values())
        {
            for (Rank rank : Rank.values())
            {
                db.sql("""
                    INSERT INTO card_definition(suit, rank)
                    VALUES (:suit, :rank)
                    ON CONFLICT (suit, rank) DO NOTHING
                    """)
                        .param("suit", suit.toShort())
                        .param("rank", rank.toShort())
                        .update();
            }
        }
    }

    public List<CardDefinition> getAll()
    {
        return db.sql("SELECT id, suit, rank FROM card_definition ORDER BY suit, rank")
                .query((rs, rowNum) -> new CardDefinition(
                        rs.getShort("id"),
                        Suit.fromShort(rs.getShort("suit")),
                        Rank.fromShort(rs.getShort("rank"))
                ))
                .list();
    }

    public Optional<CardDefinition> getById(short id)
    {
        return db.sql("SELECT id, suit, rank FROM card_definition WHERE id=:id")
                .param("id", id)
                .query((rs, rowNum) -> new CardDefinition(
                        rs.getShort("id"),
                        Suit.fromShort(rs.getShort("suit")),
                        Rank.fromShort(rs.getShort("rank"))
                ))
                .optional();
    }
}
