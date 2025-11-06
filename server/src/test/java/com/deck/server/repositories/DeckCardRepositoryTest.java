package com.deck.server.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeckCardRepositoryTest {

    @Autowired private CardDefinitionRepository cards;
    @Autowired private DeckCardRepository deckCards;
    @Autowired private DeckRepository decks;

    private UUID deckId;
    private short cardDefId;

    @BeforeEach
    void setup() {
        cards.populateAll();
        deckId = decks.createDeck("test deck");  // ✅ valid FK
        cardDefId = ((Number) cards.getAll().getFirst().get("id")).shortValue();
    }

    @Test
    void addAndCountCards() {
        UUID id = deckCards.addCardToDeck(deckId, cardDefId);
        assertThat(id).isNotNull();

        int count = deckCards.countCardsInDeck(deckId);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getAndRemoveCards() {
        UUID id = deckCards.addCardToDeck(deckId, cardDefId);

        List<Map<String, Object>> inDeck = deckCards.getCardsInDeck(deckId);
        assertThat(inDeck).hasSize(1);

        deckCards.removeCardFromDeck(id);
        assertThat(deckCards.countCardsInDeck(deckId)).isZero();
    }

    @Test
    void clearDeck() {
        deckCards.addCardToDeck(deckId, cardDefId);
        deckCards.addCardToDeck(deckId, cardDefId);
        deckCards.clearAllCardsFromDeck(deckId);

        assertThat(deckCards.countCardsInDeck(deckId)).isZero();
    }
}
