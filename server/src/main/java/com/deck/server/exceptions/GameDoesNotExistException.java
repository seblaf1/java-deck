package com.deck.server.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class GameDoesNotExistException extends CardsExceptionBase
{
    public GameDoesNotExistException(UUID gameId)
    {
        super("Game with id " + gameId + "does not exist", HttpStatus.NOT_FOUND);
    }
}
