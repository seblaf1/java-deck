package com.deck.server.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class GameRepositoryTest {

    @Autowired
    private JdbcClient db;

    @Test
    void testCreateAndDeleteGame() {
        GameRepository repo = new GameRepository(db);

        UUID id = repo.createGame();
        assertThat(repo.doesGameExist(id)).isTrue();

        repo.deleteGame(id);
        assertThat(repo.doesGameExist(id)).isFalse();
    }
}
