package com.deck.server.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserAlreadyExistsException extends CardsExceptionBase
{
    public UserAlreadyExistsException(String username)
    {
        super("User with name " + username + " already exists.", HttpStatus.CONFLICT);
    }
}
