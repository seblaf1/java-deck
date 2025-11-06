package com.deck.server.repositories;

import com.deck.server.entity.DeckCardEntity;
import com.deck.server.entity.GameCardEntity;
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
class GameCardRepositoryTest
{
    @Autowired private DeckRepository decks;
    @Autowired private CardDefinitionRepository cardDefs;
    @Autowired private DeckCardRepository deckCards;
    @Autowired private GameRepository games;
    @Autowired private GameCardRepository gameCards;

    private UUID gameId;
    private UUID deckId;

    @BeforeEach
    void setup()
    {
        cardDefs.populateAll();
        deckId = decks.createDeck("Test Deck");
        gameId = games.createGame();

        // Add first few cards from definitions
        for (var def : cardDefs.getAll().subList(0, 4))
        {
            UUID deckCardId = deckCards.addCardToDeck(deckId, def.id());
            gameCards.addCardToShoe(gameId, deckCardId, def.id());
        }
    }

    @Test
    void swapCardsInPlace()
    {
        // Arrange
        var before = gameCards.getShoeCards(gameId);
        assertThat(before).hasSize(4);

        GameCardEntity first = before.get(0);
        GameCardEntity second = before.get(1);

        long orderA = first.order_key();
        long orderB = second.order_key();

        // Act
        gameCards.swapCardsInPlace(gameId, first.card_id(), orderA, second.card_id(), orderB);

        // Assert
        var after = gameCards.getShoeCards(gameId);
        assertThat(after).hasSize(4);

        // IDs remain the same but order keys swapped
        Optional<GameCardEntity> aAfter = after.stream()
                .filter(c -> c.card_id().equals(first.card_id()))
                .findFirst();
        Optional<GameCardEntity> bAfter = after.stream()
                .filter(c -> c.card_id().equals(second.card_id()))
                .findFirst();

        assertThat(aAfter).isPresent();
        assertThat(bAfter).isPresent();

        assertThat(aAfter.get().order_key()).isEqualTo(orderB);
        assertThat(bAfter.get().order_key()).isEqualTo(orderA);

        // Verify all order_keys remain unique
        var allOrders = after.stream().map(GameCardEntity::order_key).toList();
        assertThat(allOrders).doesNotHaveDuplicates();
    }
}
