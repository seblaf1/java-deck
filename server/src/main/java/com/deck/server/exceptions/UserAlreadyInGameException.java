package com.deck.server.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserAlreadyInGameException extends CardsExceptionBase
{
    public UserAlreadyInGameException(UUID userId, UUID gameId)
    {
        super("User " + userId + " is already in game " + gameId + ".", HttpStatus.BAD_REQUEST);
    }
}
