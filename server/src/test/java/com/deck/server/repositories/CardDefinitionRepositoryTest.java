package com.deck.server.repositories;

import com.deck.server.entity.CardDefinition;
import com.deck.server.entity.Rank;
import com.deck.server.entity.Suit;
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
class CardDefinitionRepositoryTest
{
    @Autowired private CardRepository cards;

    @Test
    void populateAndReadCards()
    {
        cards.populateAll();
        List<CardDefinition> all = cards.getAll();

        assertThat(all).hasSize(52);

        CardDefinition first = all.getFirst();
        assertThat(first.suit()).isInstanceOf(Suit.class);
        assertThat(first.rank()).isInstanceOf(Rank.class);
        assertThat(first.rank().toShort()).isBetween((short)1, (short)13);
    }

    @Test
    void getByIdWorks()
    {
        cards.populateAll();
        short firstId = cards.getAll().getFirst().id();

        Optional<CardDefinition> found = cards.getById(firstId);
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(firstId);
    }
}
