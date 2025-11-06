package com.deck.server.controllers;

import com.deck.server.dto.CardCountDto;
import com.deck.server.dto.PlayerDto;
import com.deck.server.dto.SuitCountDto;
import com.deck.server.exceptions.CardsExceptionBase;
import com.deck.server.services.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameController
{
    private final GameService _gameService;

    public GameController(GameService games)
    {
        this._gameService = games;
    }

    @PostMapping
    public ResponseEntity<UUID> createGame()
    {
        try
        {
            UUID gameId = _gameService.createGame();
            return ResponseEntity.ok(gameId);
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

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> deleteGame(@PathVariable UUID gameId)
    {
        try
        {
            _gameService.deleteGame(gameId);
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

    @PostMapping("/{gameId}/decks/{deckId}")
    public ResponseEntity<Void> addDeckToGame(@PathVariable UUID gameId, @PathVariable UUID deckId)
    {
        try
        {
            _gameService.addDeckToGame(gameId, deckId);
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

    @PostMapping("/{gameId}/players/{playerId}/deal")
    public void dealCardsToPlayer(
            @PathVariable UUID gameId,
            @PathVariable UUID playerId,
            @RequestParam(defaultValue = "1") int count)
    {
        try
        {
            _gameService.dealCardsToPlayer(gameId, playerId, count);
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

    @GetMapping("/{gameId}/players")
    public ResponseEntity<List<PlayerDto>> getPlayers(@PathVariable UUID gameId)
    {
        try
        {
            var players = _gameService.getPlayersInGame(gameId);
            return ResponseEntity.ok(players);
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
     * Get the count of how many cards per suit are left undealt in the game deck.
     * Example: 5 hearts, 3 spades, etc.
     */
    @GetMapping("/{gameId}/remaining-by-suit")
    public ResponseEntity<List<SuitCountDto>> getRemainingCardsBySuit(@PathVariable UUID gameId)
    {
        try
        {
            var response = _gameService.getRemainingCardsBySuit(gameId);
            return ResponseEntity.ok(response);
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

    @GetMapping("/{gameId}/remaining-by-suit-rank")
    public ResponseEntity<List<CardCountDto>> getRemainingCardsBySuitAndRank(@PathVariable UUID gameId)
    {
        try
        {
            var result = _gameService.getRemainingCardsBySuitAndRank(gameId);
            return ResponseEntity.ok(result);
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

    @PostMapping("/{gameId}/shuffle")
    public void shuffleShoe(@PathVariable UUID gameId)
    {
        try
        {
            _gameService.shuffleShoeForGame(gameId);
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
