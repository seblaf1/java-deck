package com.deck.server.services;

import com.deck.server.repositories.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GameServiceIntegrationTest
{
    @Autowired private GameService service;
    @Autowired private IGameRepository games;
    @Autowired private IDeckRepository decks;
    @Autowired private ICardRepository cards;
    @Autowired private IUserRepository users;
    @Autowired private IPlayerRepository players;

    private UUID gameId;
    private UUID deckId;

    @BeforeEach
    void setup()
    {
        cards.populateAll();
        gameId = service.createGame();
        deckId = decks.createDeck("Test Deck");

        for (var def : cards.getAll())
            decks.addCardToDeck(deckId, def.id());
    }

    @Test
    void createAndDeleteGameWorks()
    {
        UUID newGame = service.createGame();
        assertThat(newGame).isNotNull();

        service.deleteGame(newGame);
        assertThat(games.doesGameExist(newGame)).isFalse();
    }

    @Test
    void addAndRemovePlayerWorks()
    {
        UUID userId = users.createUser("Alice");
        UUID playerId = service.addPlayerToGame(gameId, userId);

        assertThat(playerId).isNotNull();
        assertThat(players.doesPlayerExist(playerId)).isTrue();

        service.removePlayer(playerId);
        assertThat(players.doesPlayerExist(playerId)).isFalse();
    }

    @Test
    void addDeckThrowsIfEmpty()
    {
        UUID emptyDeck = decks.createDeck("Empty Deck");

        assertThatThrownBy(() ->
                service.addDeckToGame(gameId, emptyDeck))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Deck is empty");
    }

    @Test
    void shuffleShoeActuallyChangesOrder()
    {
        service.addDeckToGame(gameId, deckId);
        var before = games.getShoeCards(gameId)
                .stream()
                .map(GameCardEntity::order_key)
                .toList();

        service.shuffleShoeForGame(gameId);
        var after = games.getShoeCards(gameId)
                .stream()
                .map(GameCardEntity::order_key)
                .toList();

        assertThat(after).isNotEqualTo(before);
        assertThat(after).hasSameSizeAs(before);
    }
}
