package com.deck.server.services;

import com.deck.server.entity.CardDefinition;
import com.deck.server.repositories.*;
import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class CardService
{
    private final GameRepository games;
    private final DeckRepository decks;
    private final PlayerRepository players;
    private final DeckCardRepository deckCards;
    private final CardDefinitionRepository cardDefinitions;

    public CardService(
            GameRepository games,
            DeckRepository decks,
            PlayerRepository players,
            DeckCardRepository deckCards,
            CardDefinitionRepository cardDefs)
    {
        this.games = games;
        this.decks = decks;
        this.players = players;
        this.deckCards = deckCards;
        this.cardDefinitions = cardDefs;
    }

    @PostConstruct
    public void init()
    {
        // Ensure all card definitions are populated (once globally).
        cardDefinitions.populateAll();
    }

    @Transactional
    public UUID createDeck(String name)
    {
        UUID deckId = decks.createDeck(name);

        for (var cardDefinition : cardDefinitions.getAll())
            deckCards.addCardToDeck(deckId, cardDefinition.id());

        return deckId;
    }

//    public List<CardDefinition> getPlayerHand(UUID playerId)
//    {
//    }
}
