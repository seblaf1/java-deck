package com.deck.server.exceptions;

import org.springframework.http.HttpStatus;

public class CustomException extends CardsExceptionBase
{
    public CustomException(String message, HttpStatus code)
    {
        super(message, code);
    }
}
