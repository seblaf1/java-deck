package com.deck.server.repositories;

import com.deck.server.entity.CardDefinition;
import com.deck.server.entity.DeckCardEntity;
import com.deck.server.entity.PlayerEntity;
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
class PlayerRepositoryTest
{
    @Autowired private PlayerRepository players;
    @Autowired private GameRepository games;
    @Autowired private DeckRepository decks;
    @Autowired private CardRepository cards;
    @Autowired private org.springframework.jdbc.core.simple.JdbcClient db;

    private UUID gameId;
    private UUID deckId;
    private UUID userId;
    private List<CardDefinition> defs;

    @BeforeEach
    void setup()
    {
        // populate base 52 cards
        cards.populateAll();
        defs = cards.getAll();
        assertThat(defs).isNotEmpty();

        // create base entities
        deckId = decks.createDeck("Test Deck");
        gameId = games.createGame();

        // create one user (app_user)
        userId = UUID.randomUUID();
        db.sql("INSERT INTO app_user(id, name) VALUES (:id, :name)")
                .param("id", userId)
                .param("name", "Alice")
                .update();
    }

    @Test
    void addAndFetchPlayer_ShouldWork()
    {
        UUID playerId = players.addPlayerToGame(gameId, userId);
        assertThat(playerId).isNotNull();

        List<PlayerEntity> all = players.getAllPlayersInGame(gameId);
        assertThat(all).hasSize(1);

        PlayerEntity p = all.getFirst();
        assertThat(p.gameId()).isEqualTo(gameId);
        assertThat(p.userId()).isEqualTo(userId);
        assertThat(p.userName()).isEqualTo("Alice");
        assertThat(p.addedAt()).isNotNull();
        assertThat(players.doesPlayerExist(p.id())).isTrue();
    }

    @Test
    void removePlayer_ShouldDeleteSuccessfully()
    {
        UUID playerId = players.addPlayerToGame(gameId, userId);
        assertThat(players.doesPlayerExist(playerId)).isTrue();

        players.removePlayerFromGame(playerId);
        assertThat(players.doesPlayerExist(playerId)).isFalse();
    }

    @Test
    void addCardsToPlayerHand_ShouldAddAndRetrieveCards()
    {
        UUID playerId = players.addPlayerToGame(gameId, userId);

        UUID deckCard1 = decks.addCardToDeck(deckId, defs.get(0).id());
        UUID deckCard2 = decks.addCardToDeck(deckId, defs.get(1).id());

        DeckCardEntity c1 = new DeckCardEntity(deckCard1, deckId, defs.get(0).id());
        DeckCardEntity c2 = new DeckCardEntity(deckCard2, deckId, defs.get(1).id());
        players.addCardsToPlayerHand(playerId, List.of(c1, c2));

        List<CardDefinition> hand = players.getHandForPlayer(playerId);
        assertThat(hand).hasSize(2);
        assertThat(hand.get(0).id()).isEqualTo(defs.get(0).id());
        assertThat(hand.get(1).id()).isEqualTo(defs.get(1).id());
    }

    @Test
    void addCardsToPlayerHand_ShouldRespectOrder()
    {
        UUID playerId = players.addPlayerToGame(gameId, userId);

        UUID deckCard1 = decks.addCardToDeck(deckId, defs.get(0).id());
        UUID deckCard2 = decks.addCardToDeck(deckId, defs.get(1).id());

        DeckCardEntity c1 = new DeckCardEntity(deckCard1, deckId, defs.get(0).id());
        DeckCardEntity c2 = new DeckCardEntity(deckCard2, deckId, defs.get(1).id());
        players.addCardsToPlayerHand(playerId, List.of(c1, c2));

        UUID deckCard3 = decks.addCardToDeck(deckId, defs.get(2).id());
        DeckCardEntity c3 = new DeckCardEntity(deckCard3, deckId, defs.get(2).id());
        players.addCardsToPlayerHand(playerId, List.of(c3));

        List<CardDefinition> hand = players.getHandForPlayer(playerId);
        assertThat(hand).hasSize(3);
        assertThat(hand.get(0).id()).isEqualTo(defs.get(0).id());
        assertThat(hand.get(1).id()).isEqualTo(defs.get(1).id());
        assertThat(hand.get(2).id()).isEqualTo(defs.get(2).id());
    }
}
