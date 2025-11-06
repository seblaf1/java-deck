package com.deck.server.services;

import com.deck.server.dto.CardCountDto;
import com.deck.server.entity.CardDefinition;
import com.deck.server.entity.DeckCardEntity;
import com.deck.server.entity.PlayerEntity;
import com.deck.server.entity.Suit;
import com.deck.server.dto.PlayerDto;
import com.deck.server.dto.SuitCountDto;
import com.deck.server.exceptions.*;
import com.deck.server.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService
{
    private final ICardRepository _cardRepo;
    private final IDeckRepository _deckRepo;
    private final IPlayerRepository _playerRepo;
    private final IGameRepository _gameRepo;

    public GameService(
            ICardRepository cardRepo,
            IDeckRepository deckRepo,
            IPlayerRepository playerRepo,
            IGameRepository gameRepo)
    {
        this._cardRepo = cardRepo;
        this._deckRepo = deckRepo;
        this._playerRepo = playerRepo;
        this._gameRepo = gameRepo;
    }

    @Transactional
    public UUID createGame() throws CardsExceptionBase
    {
        return _gameRepo.createGame();
    }

    @Transactional
    public void deleteGame(UUID gameId) throws GameDoesNotExistException
    {
        if (!_gameRepo.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);

        _gameRepo.deleteGame(gameId);
    }

    @Transactional
    public UUID addPlayerToGame(UUID gameId, UUID userId) throws GameDoesNotExistException
    {
        if (!_gameRepo.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);

        // TODO: If user not exist, create

        return _playerRepo.addPlayerToGame(gameId, userId);
    }

    @Transactional
    public void removePlayer(UUID playerId) throws PlayerDoesNotExistException
    {
        if (!_playerRepo.doesPlayerExist(playerId)) throw new PlayerDoesNotExistException(playerId);

        _playerRepo.removePlayerFromGame(playerId);
    }

    @Transactional
    public void dealCardsToPlayer(UUID gameId, UUID playerId, int count) throws CustomException
    {
        if (count <= 0) throw new CustomException("count must be positive", HttpStatus.BAD_REQUEST);

        var cards = _gameRepo.popCardsFromShoe(gameId, count);
        if (cards.isEmpty()) throw new CustomException("No cards remaining in shoe", HttpStatus.OK);

        _playerRepo.addCardsToPlayerHand(playerId, cards);
    }

    public List<PlayerDto> getPlayersInGame(UUID gameId) throws GameDoesNotExistException
    {
        if (!_gameRepo.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);

        var entities = _playerRepo.getAllPlayersInGame(gameId);
        List<PlayerDto> result = new ArrayList<>(entities.size());

        for (PlayerEntity player : entities)
        {
            UUID playerId = player.id();
            List<CardDefinition> hand = _playerRepo.getHandForPlayer(playerId);

            int sum = 0;
            for (CardDefinition card : hand)
                sum += card.rank().toShort();

            result.add(new PlayerDto(playerId, player.userName(), sum));
        }

        // Sort descending by total value
        result.sort(Comparator.comparingInt(PlayerDto::totalValue).reversed());

        return result;
    }

    public List<SuitCountDto> getRemainingCardsBySuit(UUID gameId) throws GameDoesNotExistException
    {
        if (!_gameRepo.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);

        var cards = _gameRepo.getShoeCards(gameId);
        if (cards.isEmpty()) return List.of(); // TODO

        // Count per suit
        EnumMap<Suit, Integer> counts = new EnumMap<>(Suit.class);
        for (CardDefinition def : cards)
            counts.merge(def.suit(), 1, Integer::sum);

        // Map to DTOs
        List<SuitCountDto> result = new ArrayList<>(counts.size());
        for (var entry : counts.entrySet())
            result.add(new SuitCountDto(entry.getKey().toString(), entry.getValue()));

        return result;
    }

    public List<CardCountDto> getRemainingCardsBySuitAndRank(UUID gameId) throws GameDoesNotExistException
    {
        if (!_gameRepo.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);

        var cards = _gameRepo.getShoeCards(gameId);
        if (cards.isEmpty()) return List.of(); // TODO?

        // Count remaining cards grouped by (suit, rank)
        Map<String, Long> counts = cards.stream()
                .collect(Collectors.groupingBy(
                        c -> c.suit().ordinal() + ":" + c.rank().ordinal(),
                        Collectors.counting()
                ));

        // Convert to DTOs
        List<CardCountDto> result = new ArrayList<>();
        for (var entry : counts.entrySet())
        {
            var parts = entry.getKey().split(":");
            int suit = Integer.parseInt(parts[0]);
            int rank = Integer.parseInt(parts[1]);
            result.add(new CardCountDto(suit, rank, entry.getValue().intValue()));
        }

        // Sort: Hearts, Spades, Clubs, Diamonds then K → Q → J → 10 → ... → 2 → A
        result.sort(Comparator
                .comparingInt((CardCountDto c) -> c.suit() + 1)
                .thenComparingInt(c -> 14 - c.rank()));

        return result;
    }

    @Transactional
    public UUID createDeck(String name)
    {
        UUID deckId = _deckRepo.createDeck(name);

        for (var cardDefinition : _cardRepo.getAll())
            _deckRepo.addCardToDeck(deckId, cardDefinition.id());

        return deckId;
    }


    @Transactional
    public void addDeckToGame(UUID gameId, UUID deckId) throws CardsExceptionBase
    {
        if (!_gameRepo.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);
        if (!_deckRepo.doesDeckExist(deckId)) throw new DeckDoesNotExistException(deckId);

        var cards = _deckRepo.getCardsInDeck(deckId);
        if (cards.isEmpty()) throw new EmptyDeckException(deckId);

        for (DeckCardEntity card : cards)
            _gameRepo.pushbackCardToShoe(gameId, card.id());
    }

    public List<CardDefinition> getPlayerHand(UUID playerId) throws CardsExceptionBase
    {
        if (!_playerRepo.doesPlayerExist(playerId)) throw new PlayerDoesNotExistException(playerId);
        return _playerRepo.getHandForPlayer(playerId);
    }

    @Transactional
    public void shuffleShoeForGame(UUID gameId) throws GameDoesNotExistException
    {
        if (!_gameRepo.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);
        _gameRepo.shuffleShoe(gameId);
    }
}
