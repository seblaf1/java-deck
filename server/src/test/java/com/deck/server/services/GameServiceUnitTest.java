package com.deck.server.services;

import com.deck.server.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class GameServiceUnitTest {

    @Mock private IGameRepository games;
    @Mock private IDeckRepository decks;
    @Mock private IDeckCardRepository deckCards;
    @Mock private ICardRepository cardDefs;
    @Mock private IGameCardRepository shoeCards;
    @Mock private IUserRepository users;
    @Mock private IPlayerRepository players;

    @InjectMocks private GameService service;

    private UUID gameId;
    private UUID deckId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        gameId = UUID.randomUUID();
        deckId = UUID.randomUUID();
    }

    @Test
    void createGameReturnsUuid() {
        UUID expected = UUID.randomUUID();
        when(games.createGame()).thenReturn(expected);

        UUID result = service.createGame();

        assertThat(result).isEqualTo(expected);
        verify(games).createGame();
    }

    @Test
    void addDeckThrowsIfEmpty() {
        when(deckCards.getCardsInDeck(deckId)).thenReturn(Collections.emptyList());

        try {
            service.addDeckToGame(gameId, deckId);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).contains("Deck is empty");
        }
    }
}
