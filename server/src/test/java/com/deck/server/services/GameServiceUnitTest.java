package com.deck.server.services;

import com.deck.server.dto.*;
import com.deck.server.entity.*;
import com.deck.server.exceptions.*;
import com.deck.server.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GameServiceUnitTest
{
    private ICardRepository cardRepo;
    private IDeckRepository deckRepo;
    private IPlayerRepository playerRepo;
    private IGameRepository gameRepo;
    private UserRepository userRepo;
    private GameService service;

    @BeforeEach
    void setup()
    {
        cardRepo = mock(ICardRepository.class);
        deckRepo = mock(IDeckRepository.class);
        playerRepo = mock(IPlayerRepository.class);
        gameRepo = mock(IGameRepository.class);
        userRepo = mock(UserRepository.class);
        service = new GameService(cardRepo, deckRepo, playerRepo, gameRepo, userRepo);
    }

    @Test
    void createGameDelegatesToRepository() throws CardsExceptionBase
    {
        UUID gid = UUID.randomUUID();
        when(gameRepo.createGame()).thenReturn(gid);

        UUID result = service.createGame();

        assertThat(result).isEqualTo(gid);
        verify(gameRepo).createGame();
    }

    @Test
    void deleteGameThrowsWhenMissing()
    {
        UUID gid = UUID.randomUUID();
        when(gameRepo.doesGameExist(gid)).thenReturn(false);
        assertThrows(GameDoesNotExistException.class, () -> service.deleteGame(gid));
    }

    @Test
    void addPlayerToGameCallsPlayerRepo() throws CardsExceptionBase
    {
        UUID gid = UUID.randomUUID();
        UUID uid = UUID.randomUUID();
        when(gameRepo.doesGameExist(gid)).thenReturn(true);
        when(userRepo.doesUserExist(uid)).thenReturn(true);
        UUID pid = UUID.randomUUID();
        when(playerRepo.addPlayerToGame(gid, uid)).thenReturn(pid);

        UUID result = service.addPlayerToGame(gid, uid);

        assertThat(result).isEqualTo(pid);
        verify(playerRepo).addPlayerToGame(gid, uid);
    }

    @Test
    void dealCardsToPlayerAddsCards() throws CardsExceptionBase
    {
        UUID gid = UUID.randomUUID();
        UUID pid = UUID.randomUUID();
        when(gameRepo.doesGameExist(gid)).thenReturn(true);
        var deckCard = new DeckCardEntity(UUID.randomUUID(), UUID.randomUUID(), (short)1);
        when(gameRepo.popCardsFromShoe(gid, 2)).thenReturn(List.of(deckCard));

        service.dealCardsToPlayer(gid, pid, 2);

        verify(playerRepo).addCardsToPlayerHand(pid, List.of(deckCard));
    }

    @Test
    void getRemainingCardsBySuitCountsCorrectly() throws GameDoesNotExistException
    {
        UUID gid = UUID.randomUUID();
        when(gameRepo.doesGameExist(gid)).thenReturn(true);
        when(gameRepo.getShoeCards(gid)).thenReturn(List.of(
                new CardDefinition((short)1, Suit.HEARTS, Rank.ACE),
                new CardDefinition((short)2, Suit.HEARTS, Rank.KING)
        ));

        var result = service.getRemainingCardsBySuit(gid);

        assertThat(result).hasSize(Suit.values().length);
        assertThat(result.stream().filter(r -> r.suit().equals("HEARTS"))
                .findFirst().get().remaining()).isEqualTo(2);
    }

    @Test
    void getPlayerHandThrowsIfMissing()
    {
        UUID pid = UUID.randomUUID();
        when(playerRepo.doesPlayerExist(pid)).thenReturn(false);
        assertThrows(PlayerDoesNotExistException.class, () -> service.getPlayerHand(pid));
    }

    @Test
    void shuffleShoeForGameDelegates() throws GameDoesNotExistException
    {
        UUID gid = UUID.randomUUID();
        when(gameRepo.doesGameExist(gid)).thenReturn(true);

        service.shuffleShoeForGame(gid);

        verify(gameRepo).shuffleShoe(gid);
    }
}
