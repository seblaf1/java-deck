package com.deck.server.repositories;

import com.deck.server.entity.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class PlayerRepository implements IPlayerRepository
{
    private final JdbcClient db;

    public PlayerRepository(JdbcClient db)
    {
        this.db = db;
    }

    @Override
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

    @Override
    public void removePlayerFromGame(UUID playerId)
    {
        db.sql("DELETE FROM player WHERE id = :id")
                .param("id", playerId)
                .update();
    }

    @Override
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

    @Override
    public boolean doesPlayerExist(UUID playerId)
    {
        return db.sql("SELECT 1 FROM player WHERE id = :id")
                .param("id", playerId)
                .query(Integer.class)
                .optional()
                .isPresent();
    }

    @Override
    public List<CardDefinition> getHandForPlayer(UUID playerId)
    {
        return db.sql("""
            SELECT def.id, def.suit, def.rank
            FROM hand_card h
            JOIN deck_card d ON d.id = h.card_id
            JOIN card_definition def ON def.id = d.card_def_id
            WHERE h.player_id = ?
            ORDER BY h.hand_order
       """)
                .param(playerId)
                .query((rs, rowNum) -> new CardDefinition(
                        rs.getShort("id"),
                        Suit.fromShort(rs.getShort("suit")),
                        Rank.fromShort(rs.getShort("rank"))
                ))
                .list();
    }

    @Override
    public void addCardsToPlayerHand(UUID playerId, List<DeckCardEntity> cards)
    {
        if (cards == null || cards.isEmpty())
            return;

        int order = db.sql("""
            SELECT COALESCE(MAX(hand_order) + 1, 1)
            FROM hand_card
            WHERE player_id = :playerId
        """)
                .param("playerId", playerId)
                .query(Integer.class)
                .optional()
                .orElse(1);

        for (DeckCardEntity card : cards)
        {
            db.sql("""
                INSERT INTO hand_card (player_id, card_id, hand_order)
                VALUES (:playerId, :cardId, :handOrder)
            """)
                    .param("playerId", playerId)
                    .param("cardId", card.id())
                    .param("handOrder", order++)
                    .update();
        }
    }
}
