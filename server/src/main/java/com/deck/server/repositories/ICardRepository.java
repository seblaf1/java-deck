package com.deck.server.repositories;

import com.deck.server.entity.CardDefinition;
import java.util.List;
import java.util.Optional;

public interface ICardRepository
{
    void populateAll();
    List<CardDefinition> getAll();
    Optional<CardDefinition> getById(short id);
    List<CardDefinition> getManyById(List<Short> id);
}
