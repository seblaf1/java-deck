package com.deck.server.repositories;

import com.deck.server.entity.DeckCardEntity;
import com.deck.server.entity.DeckEntity;
import com.deck.server.entity.CardDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeckRepositoryTest
{
    @Autowired private DeckRepository decks;
    @Autowired private CardRepository cards;

    private UUID deckId;
    private List<CardDefinition> defs;

    @BeforeEach
    void setup()
    {
        // Ensure we have standard card definitions
        cards.populateAll();
        defs = cards.getAll();
        assertThat(defs).isNotEmpty();

        deckId = decks.createDeck("Test Deck");
        assertThat(deckId).isNotNull();
    }

    @Test
    void createAndFetchDeck_ShouldPersistAndRetrieve()
    {
        Optional<DeckEntity> result = decks.getDeckById(deckId);
        assertThat(result).isPresent();

        DeckEntity deck = result.get();
        assertThat(deck.id()).isEqualTo(deckId);
        assertThat(deck.name()).isEqualTo("Test Deck");
        assertThat(deck.createdAt()).isNotNull();
    }

    @Test
    void getAllDecks_ShouldIncludeCreatedDeck()
    {
        List<DeckEntity> all = decks.getAllDecks();
        assertThat(all).extracting(DeckEntity::id).contains(deckId);
    }

    @Test
    void addAndRemoveCards_ShouldReflectInQueries()
    {
        short defId = defs.get(0).id(); // ✅ Use existing card definition ID
        UUID deckCardId = decks.addCardToDeck(deckId, defId);
        assertThat(deckCardId).isNotNull();

        List<DeckCardEntity> cardsInDeck = decks.getCardsInDeck(deckId);
        assertThat(cardsInDeck).hasSize(1);
        assertThat(cardsInDeck.get(0).card_def_id()).isEqualTo(defId);

        int count = decks.countCardsInDeck(deckId);
        assertThat(count).isEqualTo(1);

        decks.removeCardFromDeck(deckCardId);
        assertThat(decks.countCardsInDeck(deckId)).isZero();
    }

    @Test
    void clearAllCardsFromDeck_ShouldRemoveAllCards()
    {
        short defId1 = defs.get(0).id();
        short defId2 = defs.get(1).id();
        decks.addCardToDeck(deckId, defId1);
        decks.addCardToDeck(deckId, defId2);

        assertThat(decks.countCardsInDeck(deckId)).isEqualTo(2);

        decks.clearAllCardsFromDeck(deckId);
        assertThat(decks.countCardsInDeck(deckId)).isZero();
    }

    @Test
    void doesDeckExist_ShouldReturnTrueForExistingDeck()
    {
        assertThat(decks.doesDeckExist(deckId)).isTrue();
        assertThat(decks.doesDeckExist(UUID.randomUUID())).isFalse();
    }

    @Test
    void isDeckInUse_ShouldBeFalseForFreshDeck()
    {
        assertThat(decks.isDeckInUse(deckId)).isFalse();
    }

    @Test
    void deleteDeck_ShouldRemoveDeck()
    {
        decks.deleteDeck(deckId);
        assertThat(decks.doesDeckExist(deckId)).isFalse();
    }
}
