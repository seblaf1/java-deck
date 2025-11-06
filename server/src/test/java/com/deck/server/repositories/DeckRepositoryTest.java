package com.deck.server.repositories;

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

    UUID deckId;

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

        var found = decks.getDeckById(deckId);
        assertThat(found).isPresent();

        var deck = found.get();
        assertThat(deck.get("id")).isEqualTo(deckId);
        assertThat(deck.get("name")).isEqualTo("Test Deck");
        assertThat(deck.get("created_at")).isNotNull();
    }

    @Test
    @Order(2)
    void renameDeckWorks()
    {
        decks.renameDeck(deckId, "Renamed Deck");

        var deck = decks.getDeckById(deckId).orElseThrow();
        assertThat(deck.get("name")).isEqualTo("Renamed Deck");
    }

    @Test
    @Order(3)
    void listDecksIncludesNewDeck()
    {
        var all = decks.getAllDecks();
        var ids = all.stream().map(m -> (UUID)m.get("id")).toList();

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
