package com.deck.server.repositories;

import com.deck.server.entity.CardDefinition;
import com.deck.server.entity.DeckCardEntity;
import com.deck.server.entity.GameEntity;
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
class GameRepositoryTest
{
    @Autowired private GameRepository games;
    @Autowired private DeckRepository decks;
    @Autowired private CardRepository cards;

    private UUID gameId;
    private UUID deckId;
    private List<CardDefinition> defs;

    @BeforeEach
    void setup()
    {
        // Ensure 52 cards exist
        cards.populateAll();
        defs = cards.getAll();
        assertThat(defs).isNotEmpty();

        // Create one deck and one game
        deckId = decks.createDeck("Test Deck");
        gameId = games.createGame();
    }

    @Test
    void createAndGetAll_ShouldWork()
    {
        assertThat(games.doesGameExist(gameId)).isTrue();

        List<GameEntity> all = games.getAll();
        assertThat(all).extracting(GameEntity::id).contains(gameId);
    }

    @Test
    void pushbackCardToShoe_ShouldInsertCard()
    {
        // Add one card to deck
        short defId = defs.get(0).id();
        UUID deckCardId = decks.addCardToDeck(deckId, defId);

        // Push it to shoe
        games.pushbackCardToShoe(gameId, deckCardId);

        // Verify
        List<CardDefinition> shoe = games.getShoeCards(gameId);
        assertThat(shoe).hasSize(1);
        assertThat(shoe.get(0).id()).isEqualTo(defId);
    }

    @Test
    void popCardsFromShoe_ShouldReturnAndRemoveCards()
    {
        // Add a few cards to deck and push to shoe
        var ids = defs.subList(0, 3).stream()
                .map(CardDefinition::id)
                .toList();

        for (short id : ids)
        {
            UUID deckCardId = decks.addCardToDeck(deckId, id);
            games.pushbackCardToShoe(gameId, deckCardId);
        }

        // Ensure shoe has 3 cards
        assertThat(games.getShoeCards(gameId)).hasSize(3);

        // Pop 2 cards
        List<DeckCardEntity> popped = games.popCardsFromShoe(gameId, 2);
        assertThat(popped).hasSize(2);

        // Ensure remaining count = 1
        assertThat(games.getShoeCards(gameId)).hasSize(1);
    }

    @Test
    void shuffleShoe_ShouldReorderCards()
    {
        // Fill shoe with 10 cards
        for (short id : defs.subList(0, 10).stream().map(CardDefinition::id).toList())
        {
            UUID deckCardId = decks.addCardToDeck(deckId, id);
            games.pushbackCardToShoe(gameId, deckCardId);
        }

        // Capture original order
        List<CardDefinition> before = games.getShoeCards(gameId);
        assertThat(before).hasSize(10);

        // Shuffle
        games.shuffleShoe(gameId);

        // Capture after
        List<CardDefinition> after = games.getShoeCards(gameId);
        assertThat(after).hasSize(10);

        // Should contain the same cards but likely in different order
        assertThat(after).containsExactlyInAnyOrderElementsOf(before);

        boolean anyMoved = false;
        for (int i = 0; i < before.size(); i++)
        {
            if (before.get(i).id() != after.get(i).id())
            {
                anyMoved = true;
                break;
            }
        }

        assertThat(anyMoved)
                .as("at least one card should change position after shuffle")
                .isTrue();
    }

    @Test
    void deleteGame_ShouldRemoveIt()
    {
        games.deleteGame(gameId);
        assertThat(games.doesGameExist(gameId)).isFalse();
    }
}
