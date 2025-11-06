package com.deck.server.services;

import com.deck.server.dto.CardCountDto;
import com.deck.server.dto.GameDto;
import com.deck.server.entity.*;
import com.deck.server.dto.PlayerDto;
import com.deck.server.dto.SuitCountDto;
import com.deck.server.exceptions.*;
import com.deck.server.repositories.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService
{
    private final ICardRepository cardRepository;
    private final IDeckRepository deckRepository;
    private final IPlayerRepository playerRepository;
    private final IGameRepository gameRepository;
    private final UserRepository userRepository;

    public GameService(
            ICardRepository cardRepository,
            IDeckRepository deckRepository,
            IPlayerRepository playerRepository,
            IGameRepository gameRepository, UserRepository userRepository)
    {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init()
    {
        cardRepository.populateAll();
    }

    public List<GameDto> getAllGames()
    {
        return gameRepository.getAll()
                .stream()
                .map(entity -> new GameDto(entity.id(), entity.createdAt()))
                .toList();
    }


    @Transactional
    public UUID createGame() throws CardsExceptionBase
    {
        return gameRepository.createGame();
    }

    @Transactional
    public void deleteGame(UUID gameId) throws GameDoesNotExistException
    {
        if (!gameRepository.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);

        gameRepository.deleteGame(gameId);
    }

    @Transactional
    public UUID addPlayerToGame(UUID gameId, UUID userId) throws CardsExceptionBase
    {
        if (!gameRepository.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);
        if (!userRepository.doesUserExist(userId)) throw new UserDoesNotExistException(userId);

       if (playerRepository
               .getAllPlayersInGame(gameId).stream()
               .anyMatch(p -> p.userId().equals(userId)))
       {
           throw new UserAlreadyInGameException(userId, gameId);
       }

        return playerRepository.addPlayerToGame(gameId, userId);
    }

    @Transactional
    public void removePlayer(UUID playerId) throws PlayerDoesNotExistException
    {
        if (!playerRepository.doesPlayerExist(playerId)) throw new PlayerDoesNotExistException(playerId);

        playerRepository.removePlayerFromGame(playerId);
    }

    @Transactional
    public void dealCardsToPlayer(UUID gameId, UUID playerId, int count) throws CardsExceptionBase
    {
        if (count <= 0) throw new CountMustBePositiveException(count);
        if (!gameRepository.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);

        var cards = gameRepository.popCardsFromShoe(gameId, count);
        if (cards.isEmpty()) return; // ok

        playerRepository.addCardsToPlayerHand(playerId, cards);
    }

    public List<PlayerDto> getPlayersInGame(UUID gameId) throws GameDoesNotExistException
    {
        if (!gameRepository.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);

        var entities = playerRepository.getAllPlayersInGame(gameId);
        List<PlayerDto> result = new ArrayList<>(entities.size());

        for (PlayerEntity player : entities)
        {
            UUID playerId = player.id();
            List<CardDefinition> hand = playerRepository.getHandForPlayer(playerId);

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
        if (!gameRepository.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);

        var cards = gameRepository.getShoeCards(gameId);

        // Count per suit
        EnumMap<Suit, Integer> counts = new EnumMap<>(Suit.class);
        for (CardDefinition def : cards)
            counts.merge(def.suit(), 1, Integer::sum);

        // Build result including all suits, even those not present
        List<SuitCountDto> result = new ArrayList<>(Suit.values().length);
        for (Suit suit : Suit.values())
        {
            int count = counts.getOrDefault(suit, 0);
            result.add(new SuitCountDto(suit.toString(), count));
        }

        return result;
    }

    public List<CardCountDto> getRemainingCardsBySuitAndRank(UUID gameId) throws GameDoesNotExistException
    {
        if (!gameRepository.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);

        var cards = gameRepository.getShoeCards(gameId);

        // Count remaining cards grouped by (suit, rank)
        Map<String, Long> counts = cards.stream()
                .collect(Collectors.groupingBy(
                        c -> c.suit().ordinal() + ":" + c.rank().ordinal(),
                        Collectors.counting()
                ));

        // Build result with every suit/rank combination explicitly present
        List<CardCountDto> result = new ArrayList<>(Suit.values().length * Rank.values().length);
        for (Suit suit : Suit.values())
        {
            for (Rank rank : Rank.values())
            {
                String key = suit.ordinal() + ":" + rank.ordinal();
                int count = counts.getOrDefault(key, 0L).intValue();
                result.add(new CardCountDto(suit.ordinal(), rank.ordinal(), count));
            }
        }

        // Sort: Hearts, Spades, Clubs, Diamonds then K → Q → J → 10 → ... → 2 → A
        result.sort(Comparator
                .comparingInt(CardCountDto::suit)
                .thenComparingInt(c -> 14 - c.rank()));

        return result;
    }

    @Transactional
    public UUID createDeck(String name)
    {
        UUID deckId = deckRepository.createDeck(name);

        for (var cardDefinition : cardRepository.getAll())
            deckRepository.addCardToDeck(deckId, cardDefinition.id());

        return deckId;
    }


    @Transactional
    public void addDeckToGame(UUID gameId, UUID deckId) throws CardsExceptionBase
    {
        if (!gameRepository.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);
        if (!deckRepository.doesDeckExist(deckId)) throw new DeckDoesNotExistException(deckId);

        var cards = deckRepository.getCardsInDeck(deckId);
        if (cards.isEmpty()) throw new EmptyDeckException(deckId);

        for (DeckCardEntity card : cards)
            gameRepository.pushbackCardToShoe(gameId, card.id());
    }

    public List<CardDefinition> getPlayerHand(UUID playerId) throws CardsExceptionBase
    {
        if (!playerRepository.doesPlayerExist(playerId)) throw new PlayerDoesNotExistException(playerId);
        return playerRepository.getHandForPlayer(playerId);
    }

    @Transactional
    public void shuffleShoeForGame(UUID gameId) throws GameDoesNotExistException
    {
        if (!gameRepository.doesGameExist(gameId)) throw new GameDoesNotExistException(gameId);
        gameRepository.shuffleShoe(gameId);
    }
}
