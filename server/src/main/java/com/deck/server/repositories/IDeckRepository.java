package com.deck.server.repositories;

import com.deck.server.entity.DeckCardEntity;
import com.deck.server.entity.DeckEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDeckRepository
{
    UUID createDeck(String name);
    Optional<DeckEntity> getDeckById(UUID deckId);
    List<DeckEntity> getAllDecks();
    void deleteDeck(UUID deckId);
    boolean isDeckInUse(UUID deckId);
    boolean doesDeckExist(UUID deckId);

    UUID addCardToDeck(UUID deckId, short cardDefId);
    List<DeckCardEntity> getCardsInDeck(UUID deckId);
    void removeCardFromDeck(UUID deckCardId);
    void clearAllCardsFromDeck(UUID deckId);
    int countCardsInDeck(UUID deckId);
}
