package com.deck.server.repositories;

import com.deck.server.entity.DeckCardEntity;
import com.deck.server.entity.DeckEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeckRepositoryTest
{
    @Autowired private CardRepository cards;
    @Autowired private DeckRepository decks;
    private UUID deckId;
    private short cardDefId;

    @BeforeEach
    void setup()
    {
        cards.populateAll();
        deckId = decks.createDeck("test deck");  // ✅ valid FK
        cardDefId = ((Number) cards.getAll().getFirst().id()).shortValue();
    }

    @Test
    void createDeckWorks()
    {
        assertThat(deckId).isNotNull();

        Optional<DeckEntity> found = decks.getDeckById(deckId);
        assertThat(found).isPresent();

        DeckEntity deck = found.get();
        assertThat(deck.id()).isEqualTo(deckId);
        assertThat(deck.name()).isEqualTo("Test Deck");
        assertThat(deck.createdAt()).isNotNull();
    }

    @Test
    void listDecksIncludesNewDeck()
    {
        List<DeckEntity> all = decks.getAllDecks();
        List<UUID> ids = all.stream().map(DeckEntity::id).toList();

        assertThat(ids).contains(deckId);
    }

    @Test
    void deleteDeckWorks()
    {
        decks.deleteDeck(deckId);
        assertThat(decks.getDeckById(deckId)).isEmpty();
    }

    @Test
    void addAndCountCards()
    {
        UUID id = decks.addCardToDeck(deckId, cardDefId);
        assertThat(id).isNotNull();

        int count = decks.countCardsInDeck(deckId);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getAndRemoveCards()
    {
        UUID id = decks.addCardToDeck(deckId, cardDefId);

        List<DeckCardEntity> inDeck = decks.getCardsInDeck(deckId);
        assertThat(inDeck).hasSize(1);
        assertThat(inDeck.getFirst().deck_id()).isEqualTo(deckId);
        assertThat(inDeck.getFirst().card_def_id()).isEqualTo(cardDefId);

        decks.removeCardFromDeck(id);
        assertThat(decks.countCardsInDeck(deckId)).isZero();
    }

    @Test
    void clearDeck()
    {
        decks.addCardToDeck(deckId, cardDefId);
        decks.addCardToDeck(deckId, cardDefId);
        decks.clearAllCardsFromDeck(deckId);

        assertThat(decks.countCardsInDeck(deckId)).isZero();
    }
}
