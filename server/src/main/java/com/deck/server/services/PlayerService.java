package com.deck.server.services;

import com.deck.server.dto.CardDto;
import com.deck.server.exceptions.PlayerDoesNotExistException;
import com.deck.server.repositories.IPlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PlayerService
{
    private final IPlayerRepository _playerRepo;

    public PlayerService(
            IPlayerRepository playerRepo)
    {
        this._playerRepo = playerRepo;
    }

    public List<CardDto> getCardsForPlayer(UUID playerId) throws PlayerDoesNotExistException
    {
        if (_playerRepo.doesPlayerExist(playerId)) throw new PlayerDoesNotExistException(playerId);

        return _playerRepo.getHandForPlayer(playerId)
                .stream()
                .map(CardDto::fromDefinition)
                .toList();
    }
}
