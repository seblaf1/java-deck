package com.deck.server.repositories;

import com.deck.server.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserRepository
{
    UUID createUser(String name);
    Optional<User> getUserByName(String name);
    boolean doesUserExist(UUID userId);
    void deleteUser(UUID userId);
    List<User> getAllUsers();
}
