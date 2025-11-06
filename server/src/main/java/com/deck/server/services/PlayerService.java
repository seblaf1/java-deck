package com.deck.server.services;

import com.deck.server.dto.CardDto;
import com.deck.server.exceptions.PlayerDoesNotExistException;
import com.deck.server.exceptions.UserAlreadyExistsException;
import com.deck.server.repositories.IPlayerRepository;
import com.deck.server.repositories.IUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PlayerService
{
    private final IPlayerRepository playerRepository;
    private final IUserRepository userRepository;

    public PlayerService(IPlayerRepository playerRepository, IUserRepository userRepository)
    {
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user and returns its ID.
     * If a user with the same name already exists, throws a UserAlreadyExistsException.
     */
    @Transactional
    public UUID createUser(String name) throws UserAlreadyExistsException
    {
        if (userRepository.getUserByName(name).isPresent()) throw new UserAlreadyExistsException(name);

        UUID id = UUID.randomUUID();
        return userRepository.createUser(name, id);
    }

    public List<CardDto> getCardsForPlayer(UUID playerId) throws PlayerDoesNotExistException
    {
        if (!playerRepository.doesPlayerExist(playerId)) throw new PlayerDoesNotExistException(playerId);

        return playerRepository.getHandForPlayer(playerId)
                .stream()
                .map(CardDto::fromDefinition)
                .toList();
    }
}
