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
    private final PlayerService playerService;
    private final GameService gameService;

    public PlayerController(PlayerService playerService, GameService gameService)
    {
        this.playerService = playerService;
        this.gameService = gameService;
    }

    /**
     * Create a new user (player) by name.
     */
    @PostMapping
    public ResponseEntity<UUID> createUser(@RequestParam String name)
    {
        try
        {
            UUID userId = playerService.createUser(name);
            return ResponseEntity.ok().body(userId);
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

    /**
     * Remove a player from their game.
     */
    @DeleteMapping("/{playerId}/leave")
    public ResponseEntity<Void> removePlayer(@PathVariable UUID playerId)
    {
        try
        {
            gameService.removePlayer(playerId);
            return ResponseEntity.ok().build();
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

    /**
     * Get the list of cards for a player
     */
    @GetMapping("/{playerId}/hand")
    public ResponseEntity<List<CardDto>> getListOfCardsForPlayer(@PathVariable UUID playerId)
    {
        try
        {
            var cards = playerService.getCardsForPlayer(playerId);
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
