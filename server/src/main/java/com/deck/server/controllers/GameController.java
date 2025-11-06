package com.deck.server.controllers;

import com.deck.server.dto.CardCountDto;
import com.deck.server.dto.GameDto;
import com.deck.server.dto.PlayerDto;
import com.deck.server.dto.SuitCountDto;
import com.deck.server.exceptions.CardsExceptionBase;
import com.deck.server.services.GameService;
import com.deck.server.services.PlayerService;
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
    private final GameService gameService;
    private final PlayerService playerService;

    public GameController(GameService gameService, PlayerService playerService)
    {
        this.gameService = gameService;
        this.playerService = playerService;
    }

    /**
     * Create a game
     */
    @PostMapping
    public ResponseEntity<UUID> createGame()
    {
        try
        {
            UUID gameId = gameService.createGame();
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

    /**
     * Delete a game
     */
    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> deleteGame(@PathVariable UUID gameId)
    {
        try
        {
            gameService.deleteGame(gameId);
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
     * List all games.
     */
    @GetMapping
    public ResponseEntity<List<GameDto>> listGames()
    {
        try
        {
            var games = gameService.getAllGames();
            return ResponseEntity.ok(games);
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Creates a new deck.
     */
    @PostMapping("/new-deck")
    public ResponseEntity<UUID> createDeck()
    {
        try
        {
            var deckId = gameService.createDeck("deck name placeholder");
            return ResponseEntity.ok(deckId);
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Add a deck to the game deck (shoe).
     */
    @PostMapping("/{gameId}/decks/{deckId}")
    public ResponseEntity<Void> addDeckToGame(@PathVariable UUID gameId, @PathVariable UUID deckId)
    {
        try
        {
            gameService.addDeckToGame(gameId, deckId);
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
     * Add a user as a player in a specific game.
     */
    @PostMapping("/{gameId}/join")
    public ResponseEntity<UUID> addPlayerToGame(@RequestParam String playerName, @PathVariable UUID gameId)
    {
        try
        {
            UUID userId = playerService.createUser(playerName);
            UUID playerId = gameService.addPlayerToGame(gameId, userId);
            return ResponseEntity.ok().body(playerId);
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
     * Deal cards to a player in a game from the game deck
     */
    @PostMapping("/{gameId}/players/{playerId}/deal")
    public ResponseEntity<Void> dealCardsToPlayer(
            @PathVariable UUID gameId,
            @PathVariable UUID playerId,
            @RequestParam(defaultValue = "1") int count)
    {
        try
        {
            gameService.dealCardsToPlayer(gameId, playerId, count);
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
     * Get the list of players in a game along with the total added value of all the cards each player holds
     */
    @GetMapping("/{gameId}/players")
    public ResponseEntity<List<PlayerDto>> getPlayers(@PathVariable UUID gameId)
    {
        try
        {
            var players = gameService.getPlayersInGame(gameId);
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
            var response = gameService.getRemainingCardsBySuit(gameId);
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

    /**
     * Get the count of each card (suit and value) remaining in the game deck sorted by suit
     */
    @GetMapping("/{gameId}/remaining-by-suit-rank")
    public ResponseEntity<List<CardCountDto>> getRemainingCardsBySuitAndRank(@PathVariable UUID gameId)
    {
        try
        {
            var result = gameService.getRemainingCardsBySuitAndRank(gameId);
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

    /**
     * Shuffle the game deck (shoe)
     */
    @PostMapping("/{gameId}/shuffle")
    public ResponseEntity<String> shuffleShoe(@PathVariable UUID gameId)
    {
        try
        {
            gameService.shuffleShoeForGame(gameId);
            return ResponseEntity.ok("");
        }
        catch (CardsExceptionBase ex)
        {
            return ResponseEntity.status(ex.code).body(ex.getMessage());
        }
        catch (Exception ex)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }
}
