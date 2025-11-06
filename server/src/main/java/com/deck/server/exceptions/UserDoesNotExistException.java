package com.deck.server.exceptions;

import org.springframework.http.HttpStatus;
import java.util.UUID;

public class UserDoesNotExistException extends CardsExceptionBase
{
    public UserDoesNotExistException(UUID userId)
    {
        super("User does not exist: " + userId, HttpStatus.NOT_FOUND);
    }
}
