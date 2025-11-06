package com.deck.server.repositories;

import com.deck.server.entity.CardDefinition;
import com.deck.server.entity.DeckCardEntity;
import com.deck.server.entity.PlayerEntity;

import java.util.List;
import java.util.UUID;

public interface IPlayerRepository
{
    UUID addPlayerToGame(UUID gameId, UUID userId);
    void removePlayerFromGame(UUID playerId);
    List<PlayerEntity> getAllPlayersInGame(UUID gameId);
    boolean doesPlayerExist(UUID playerId);

    List<CardDefinition> getHandForPlayer(UUID playerId);
    void addCardsToPlayerHand(UUID playerId, List<DeckCardEntity> cards);
}
