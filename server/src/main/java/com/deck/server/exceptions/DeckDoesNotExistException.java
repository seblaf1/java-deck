package com.deck.server.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class DeckDoesNotExistException extends CardsExceptionBase
{
    public DeckDoesNotExistException(UUID deckId)
    {
        super("Deck does not exist: " + deckId, HttpStatus.NOT_FOUND);
    }
}
