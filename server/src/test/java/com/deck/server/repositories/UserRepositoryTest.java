package com.deck.server.repositories;

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
class UserRepositoryTest {

    @Autowired
    private UserRepository users;

    @Test
    void createAndGetUser() {
        UUID id = users.createUser("Alice");
        assertThat(id).isNotNull();

        Optional<UUID> found = users.getUserIdByName("Alice");
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(id);
    }

    @Test
    void userExistsAndDelete() {
        UUID id = users.createUser("Bob");
        assertThat(users.doesUserExist(id)).isTrue();

        users.deleteUser(id);
        assertThat(users.doesUserExist(id)).isFalse();
    }
}
