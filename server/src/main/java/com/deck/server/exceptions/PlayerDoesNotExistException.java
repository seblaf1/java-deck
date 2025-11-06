package com.deck.server.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class PlayerDoesNotExistException extends CardsExceptionBase
{
    public PlayerDoesNotExistException(UUID playerId)
    {
        super("Player does not exist: " + playerId, HttpStatus.NOT_FOUND);
    }
}
