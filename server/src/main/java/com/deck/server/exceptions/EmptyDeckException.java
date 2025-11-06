package com.deck.server.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class EmptyDeckException extends CardsExceptionBase
{
    public EmptyDeckException(UUID deckId)
    {
        super("Deck with id " + deckId + " is empty.", HttpStatus.NO_CONTENT);
    }
}
