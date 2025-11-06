package com.deck.server.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class EmptyShoeException extends CardsExceptionBase
{
    public EmptyShoeException(UUID deckId)
    {
        super("Deck with id " + deckId + " is empty.", HttpStatus.OK);
    }
}
