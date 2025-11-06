package com.deck.server.controllers;

import com.deck.server.dto.CardDto;
import com.deck.server.exceptions.CardsExceptionBase;
import com.deck.server.services.GameService;
import com.deck.server.services.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/players")
public class PlayerController
{
    private final PlayerService _playerService;

    public PlayerController(GameService games, PlayerService playerService)
    {
        _playerService = playerService;
    }

    @GetMapping("/{playerId}/hand")
    public ResponseEntity<List<CardDto>> getListOfCardsForPlayer(@PathVariable UUID playerId)
    {
        try
        {
            var cards = _playerService.getCardsForPlayer(playerId);
            return ResponseEntity.ok(cards);
        }
        catch (CardsExceptionBase ex)
        {
            throw new ResponseStatusException(ex.code, ex.getMessage());
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
