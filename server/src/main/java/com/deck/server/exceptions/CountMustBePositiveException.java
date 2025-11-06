package com.deck.server.exceptions;

import org.springframework.http.HttpStatus;

public class CountMustBePositiveException extends CardsExceptionBase
{
    public CountMustBePositiveException(int count)
    {
        super("Count must be positive, but was " + count, HttpStatus.BAD_REQUEST);
    }
}
