package com.deck.server.repositories;

import com.deck.server.entity.CardDefinition;
import com.deck.server.entity.DeckCardEntity;
import com.deck.server.entity.Rank;
import com.deck.server.entity.Suit;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Repository
public class GameRepository implements IGameRepository
{
    private final JdbcClient db;
    public GameRepository(JdbcClient db) { this.db = db; }

    @Override
    public UUID createGame()
    {
        UUID id = UUID.randomUUID();
        db.sql("INSERT INTO game(id) VALUES (:id)")
                .param("id", id).update();
        return id;
    }

    @Override
    public void deleteGame(UUID gameId)
    {
        db.sql("DELETE FROM game WHERE id=:id").param("id", gameId).update();
    }

    @Override
    public boolean doesGameExist(UUID gameId)
    {
        return db.sql("SELECT 1 FROM game WHERE id=:id")
                .param("id", gameId).query(Integer.class).optional().isPresent();
    }

    @Override
    public void pushbackCardToShoe(UUID gameId, UUID cardId)
    {
        long orderKey = getNextShoeIndexForGame(gameId);

        db.sql("""
            INSERT INTO shoe_card (game_id, card_id, order_key)
            SELECT :gameId, :cardId, :orderKey
            WHERE NOT EXISTS (
                SELECT 1 FROM shoe_card
                WHERE game_id = :gameId AND card_id = :cardId
            )
            """)
                .param("gameId", gameId)
                .param("cardId", cardId)
                .param("orderKey", orderKey)
                .update();
    }

    @Override
    public List<CardDefinition> getShoeCards(UUID gameId)
    {
        return db.sql("""
        SELECT def.id   AS id,
               def.suit AS suit,
               def.rank AS rank
        FROM shoe_card s
        JOIN deck_card d ON d.id = s.card_id
        JOIN card_definition def ON def.id = d.card_def_id
        WHERE s.game_id = :gameId
        ORDER BY s.order_key
        """)
                .param("gameId", gameId)
                .query((rs, rowNum) -> new CardDefinition(
                        rs.getShort("id"),
                        Suit.fromShort(rs.getShort("suit")),
                        Rank.fromShort(rs.getShort("rank"))
                ))
                .list();
    }

    @Override
    public List<DeckCardEntity> popCardsFromShoe(UUID gameId, int count)
    {
        // 1. Select the top N cards from the shoe joined with their deck info
        List<DeckCardEntity> cards = db.sql("""
            SELECT d.id, d.deck_id, d.card_def_id
            FROM shoe_card s
            JOIN deck_card d ON d.id = s.card_id
            WHERE s.game_id = :gameId
            ORDER BY s.order_key
            LIMIT : count
        """)
                .param("gameId", gameId)
                .param("count", count)
                .query((rs, rowNum) -> new DeckCardEntity(
                        (UUID) rs.getObject("id"),
                        (UUID) rs.getObject("deck_id"),
                        ((Number) rs.getObject("card_def_id")).shortValue()
                ))
                .list();

        if (cards.isEmpty())
            return List.of();

        // 2. Remove those cards from the shoe
        db.sql("""
            DELETE FROM shoe_card
            WHERE game_id = :gameId
              AND card_id IN (:cardIds)
        """)
                .param("gameId", gameId)
                .param("cardIds", cards.stream().map(DeckCardEntity::id).toList())
                .update();

        return cards;
    }

    @Override
    @Transactional
    public void shuffleShoe(UUID gameId)
    {
        var cards = db.sql("""
            SELECT card_id, order_key
            FROM shoe_card
            WHERE game_id = :gameId
            ORDER BY order_key
            """)
                .param("gameId", gameId)
                .query((rs, rowNum) -> new Object[] {
                        rs.getObject("card_id", UUID.class),
                        rs.getLong("order_key")
                })
                .list();

        int n = cards.size();
        if (n <= 1)
            return;

        Random rng = new Random();

        for (int i = n - 1; i > 0; i--)
        {
            int j = rng.nextInt(i + 1);
            if (i == j) continue;

            var cardI = (UUID) cards.get(i)[0];
            long orderI = (long) cards.get(i)[1];

            var cardJ = (UUID) cards.get(j)[0];
            long orderJ = (long) cards.get(j)[1];

            // swap their order_keys atomically using the same safe temp slot method
            long tempKey = orderI + 10_000_000L;

            // move A to temp
            db.sql(
                            """
            UPDATE
                            shoe_card
                                        SET order_key = :temp
            WHERE
                            game_id = :gameId AND card_id = :cardI
            """)
                    .param("temp", tempKey)
                    .param("gameId", gameId)
                    .param("cardI", cardI)
                    .update();
                            // move B to A’s original slot
            db.sql("""
            UPDATE
                            shoe_card
            SET order_key = :orderI
                                        WHERE game_id = :gameId AND card_id = :cardJ
            """)
                    .param("orderI", orderI)
                    .param("gameId", gameId)
                    .param("cardJ", cardJ)
                    .
                            update();

            // move A to B’s old slot
            db.sql("""
            UPDATE shoe_car
                             SET order_key = :orderJ
            WHERE game_id = :gameId AND card_id = :cardI
            """)
                    .param("orderJ", orderJ)
                    .param("gameId", gameId)
                    .param("cardI", cardI)
                    .update();

            // maintain in-memory list consistency for next swaps
            cards.set(i, new Object[] { cardJ, orderJ });
            cards.set(j, new Object[] { cardI, orderI });
        }
    }

    private long getNextShoeIndexForGame(UUID gameId)
    {
        Long max = db.sql("SELECT MAX(order_key) FROM shoe_card WHERE game_id = :gameId")
                .param("gameId", gameId)
                .query(Long.class)
                .optional()
                .orElse(0L);

        return max + 1;
    }
}
