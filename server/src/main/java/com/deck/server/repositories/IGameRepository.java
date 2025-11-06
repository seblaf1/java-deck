package com.deck.server.repositories;

import com.deck.server.entity.CardDefinition;
import com.deck.server.entity.DeckCardEntity;

import java.util.List;
import java.util.UUID;

public interface IGameRepository
{
    UUID createGame();
    void deleteGame(UUID gameId);
    boolean doesGameExist(UUID gameId);

    List<CardDefinition> getShoeCards(UUID gameId);
    List<DeckCardEntity> popCardsFromShoe(UUID gameId, int count);
    void pushbackCardToShoe(UUID gameId, UUID cardId);
    void shuffleShoe(UUID gameId);
}
