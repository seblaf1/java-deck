package com.deck.server.exceptions;

import org.springframework.http.HttpStatus;

public class CardDoesNotExistException extends CardsExceptionBase
{
    public CardDoesNotExistException(short cardId)
    {
        super("Card does not exist: " + cardId, HttpStatus.NOT_FOUND);
    }
}
