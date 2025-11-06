package com.deck.server.services;

import com.deck.server.entity.DeckCardEntity;
import com.deck.server.entity.PlayerEntity;
import com.deck.server.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class GameService
{
    private final GameRepository games;
    private final DeckRepository decks;
    private final PlayerRepository players;
    private final DeckCardRepository deckCards;
    private final GameCardRepository gameCards;
    private final CardDefinitionRepository cardDefinitions;

    public GameService(
            GameRepository games,
            DeckRepository decks,
            PlayerRepository players,
            DeckCardRepository deckCards,
            GameCardRepository shoeCards,
            CardDefinitionRepository cardDefs)
    {
        this.games = games;
        this.decks = decks;
        this.players = players;
        this.deckCards = deckCards;
        this.gameCards = shoeCards;
        this.cardDefinitions = cardDefs;
    }

    @Transactional
    public UUID createGame()
    {
        return games.createGame();
    }

    @Transactional
    public void deleteGame(UUID gameId)
    {
        games.deleteGame(gameId);
    }

    @Transactional
    public UUID addPlayerToGame(UUID gameId, UUID userId)
    {
        return players.addPlayerToGame(gameId, userId);
    }

    @Transactional
    public void removePlayer(UUID playerId)
    {
        players.removePlayerFromGame(playerId);
    }

    public List<PlayerEntity> getPlayersInGame(UUID gameId)
    {
        return players.getAllPlayersInGame(gameId);
    }

    @Transactional
    public void addDeckToGame(UUID gameId, UUID deckId)
    {
        // Retrieve all deck cards
        var cards = deckCards.getCardsInDeck(deckId);
        if (cards.isEmpty())
            throw new IllegalStateException("Deck is empty; cannot add to shoe");

        long index = gameCards.getNextIndexForGame(gameId);

        for (DeckCardEntity card : cards)
        {
            gameCards.addCardToShoe(gameId, card.id(), index++);
        }
    }

    @Transactional
    public void shuffleShoeForGame(UUID gameId)
    {
        var cards = gameCards.getShoeCards(gameId);

        int n = cards.size();
        if (n <= 1)
            return; // nothing to shuffle

        Random rng = new Random();

        for (int i = n - 1; i > 0; i--)
        {
            int j = rng.nextInt(i + 1);
            if (i == j) continue;

            var cardI = cards.get(i);
            var cardJ = cards.get(j);

            gameCards.swapCardsInPlace(
                    gameId,
                    cardI.card_id(), cardI.order_key(),
                    cardJ.card_id(), cardJ.order_key()
            );

            cards.set(i, cardJ);
            cards.set(j, cardI);
        }
    }
}
