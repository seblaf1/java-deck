package com.deck.server.services;

import com.deck.server.dto.CardCountDto;
import com.deck.server.dto.SuitCountDto;
import com.deck.server.entity.CardDefinition;
import com.deck.server.entity.DeckCardEntity;
import com.deck.server.exceptions.CardsExceptionBase;
import com.deck.server.exceptions.GameDoesNotExistException;
import com.deck.server.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GameServiceIntegrationTest
{
    @Autowired private GameService service;
    @Autowired private GameRepository games;
    @Autowired private DeckRepository decks;
    @Autowired private CardRepository cards;
    @Autowired private PlayerRepository players;
    @Autowired private UserRepository users;

    private UUID gameId;
    private UUID userId;
    private UUID deckId;
    private UUID playerId;

    @BeforeEach
    void setup() throws CardsExceptionBase
    {
        UUID id = UUID.randomUUID();
        cards.populateAll();
        gameId = service.createGame();
        deckId = service.createDeck("Deck1");
        userId = users.createUser("Player1", id);
        playerId = service.addPlayerToGame(gameId, userId);
        service.addDeckToGame(gameId, deckId);
    }

    @Test
    void createAndDeleteGame() throws CardsExceptionBase
    {
        UUID newGame = service.createGame();
        assertThat(games.doesGameExist(newGame)).isTrue();
        service.deleteGame(newGame);
        assertThat(games.doesGameExist(newGame)).isFalse();
    }

    @Test
    void dealCardsAndGetPlayerHand() throws CardsExceptionBase
    {
        service.dealCardsToPlayer(gameId, playerId, 2);
        var hand = service.getPlayerHand(playerId);
        assertThat(hand).hasSize(2);
    }

    @Test
    void getPlayersInGameReturnsDto() throws GameDoesNotExistException
    {
        var players = service.getPlayersInGame(gameId);
        assertThat(players).hasSize(1);
        assertThat(players.getFirst().playerName()).isEqualTo("Player1");
    }

    @Test
    void getRemainingCardsBySuitIncludesAllSuits() throws GameDoesNotExistException
    {
        List<SuitCountDto> counts = service.getRemainingCardsBySuit(gameId);
        assertThat(counts).hasSize(4);
        assertThat(counts).allSatisfy(c -> assertThat(c.remaining()).isGreaterThanOrEqualTo(0));
    }

    @Test
    void getRemainingCardsBySuitAndRankIncludesAllCombinations() throws GameDoesNotExistException
    {
        List<CardCountDto> counts = service.getRemainingCardsBySuitAndRank(gameId);
        assertThat(counts).hasSize(52);
    }

    @Test
    void shuffleShoeDoesNotLoseCards() throws GameDoesNotExistException
    {
        var before = games.getShoeCards(gameId);
        service.shuffleShoeForGame(gameId);
        var after = games.getShoeCards(gameId);

        assertThat(after).extracting(CardDefinition::id)
                .containsExactlyInAnyOrderElementsOf(before.stream().map(CardDefinition::id).toList());
    }
}
