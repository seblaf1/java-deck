package com.deck.server.repositories;

import com.deck.server.entity.User;
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
class UserRepositoryTest
{
    @Autowired private UserRepository users;

    @Test
    void createAndGetUser()
    {
        UUID id = users.createUser("Alice");
        assertThat(id).isNotNull();

        Optional<User> found = users.getUserByName("Alice");
        assertThat(found).isPresent();

        User u = found.get();
        assertThat(u.id()).isEqualTo(id);
        assertThat(u.name()).isEqualTo("Alice");
        assertThat(u.createdAt()).isNotNull();
    }

    @Test
    void userExistsAndDelete()
    {
        UUID id = users.createUser("Bob");
        assertThat(users.doesUserExist(id)).isTrue();

        users.deleteUser(id);
        assertThat(users.doesUserExist(id)).isFalse();
    }

    @Test
    void getAllUsersIncludesCreatedOnes()
    {
        UUID id = users.createUser("Charlie");
        List<User> all = users.getAllUsers();

        assertThat(all.stream().map(User::id)).contains(id);
    }
}
