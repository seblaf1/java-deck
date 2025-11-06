package com.deck.server.exceptions;

import org.springframework.http.HttpStatus;

public abstract class CardsExceptionBase extends Exception
{
    public final HttpStatus code;

    protected CardsExceptionBase(String message, HttpStatus code)
    {
        super(message);
        this.code = code;
    }
}
