package com.deck.server.repositories;

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
    @Autowired private DeckRepository decks;
    private UUID deckId;

    @BeforeEach
    void setup()
    {
        deckId = decks.createDeck("Test Deck");
    }

    @Test
    @Order(1)
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
    @Order(2)
    void renameDeckWorks()
    {
        decks.renameDeck(deckId, "Renamed Deck");

        DeckEntity deck = decks.getDeckById(deckId).orElseThrow();
        assertThat(deck.name()).isEqualTo("Renamed Deck");
    }

    @Test
    @Order(3)
    void listDecksIncludesNewDeck()
    {
        List<DeckEntity> all = decks.getAllDecks();
        List<UUID> ids = all.stream().map(DeckEntity::id).toList();

        assertThat(ids).contains(deckId);
    }

    @Test
    @Order(4)
    void deleteDeckWorks()
    {
        decks.deleteDeck(deckId);
        assertThat(decks.getDeckById(deckId)).isEmpty();
    }
}
