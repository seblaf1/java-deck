package com.deck.server.repositories;

import com.deck.server.entity.PlayerEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlayerRepositoryTest
{
    @Autowired private GameRepository games;
    @Autowired private UserRepository users;
    @Autowired private PlayerRepository players;

    private UUID gameId;
    private UUID userId;

    @BeforeEach
    void setup()
    {
        gameId = games.createGame();
        userId = users.createUser("Charlie");
    }

    @Test
    void addAndRemovePlayer()
    {
        UUID playerId = players.addPlayerToGame(gameId, userId);
        assertThat(playerId).isNotNull();
        assertThat(players.doesPlayerExist(playerId)).isTrue();

        players.removePlayerFromGame(playerId);
        assertThat(players.doesPlayerExist(playerId)).isFalse();
    }

    @Test
    void getPlayersInGame()
    {
        players.addPlayerToGame(gameId, userId);
        List<PlayerEntity> list = players.getAllPlayersInGame(gameId);

        assertThat(list).hasSize(1);

        PlayerEntity p = list.getFirst();
        assertThat(p.userName()).isEqualTo("Charlie");
        assertThat(p.userId()).isEqualTo(userId);
        assertThat(p.gameId()).isEqualTo(gameId);
        assertThat(p.addedAt()).isNotNull();
    }
}
