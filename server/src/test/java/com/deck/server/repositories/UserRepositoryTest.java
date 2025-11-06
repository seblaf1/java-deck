package com.deck.server.repositories;

import com.deck.server.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest
{
    @Autowired private UserRepository users;

    @Test
    void createAndGetUserByName()
    {
        UUID id = UUID.randomUUID();
        users.createUser("Alice", id);
        Optional<User> found = users.getUserByName("Alice");

        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(id);
        assertThat(found.get().name()).isEqualTo("Alice");
    }

    @Test
    void doesUserExistWorks()
    {
        UUID id = UUID.randomUUID();
        users.createUser("Bob", id);
        assertThat(users.doesUserExist(id)).isTrue();
        assertThat(users.doesUserExist(UUID.randomUUID())).isFalse();
    }

    @Test
    void getAllUsersReturnsInserted()
    {
        users.createUser("C1", UUID.randomUUID());
        users.createUser("C2", UUID.randomUUID());

        List<User> all = users.getAllUsers();
        assertThat(all).extracting(User::name).contains("C1", "C2");
    }

    @Test
    void deleteUserRemovesIt()
    {
        UUID id = UUID.randomUUID();
        users.createUser("ToDelete", id);
        users.deleteUser(id);

        assertThat(users.doesUserExist(id)).isFalse();
        assertThat(users.getUserByName("ToDelete")).isEmpty();
    }
}
