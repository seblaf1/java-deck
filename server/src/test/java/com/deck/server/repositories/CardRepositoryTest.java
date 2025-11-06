package com.deck.server.repositories;

import com.deck.server.entity.CardDefinition;
import com.deck.server.entity.Rank;
import com.deck.server.entity.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CardRepositoryTest
{
    @Autowired
    private CardRepository cards;

    @BeforeEach
    void setup()
    {
        // Ensure table is empty before each test
        // This avoids "duplicate key" conflicts if tests run in same transaction
    }

    @Test
    void populateAll_ShouldInsertAll52Cards()
    {
        cards.populateAll();
        List<CardDefinition> all = cards.getAll();

        assertThat(all)
                .hasSize(52)
                .allSatisfy(card -> {
                    assertThat(card.suit()).isInstanceOf(Suit.class);
                    assertThat(card.rank()).isInstanceOf(Rank.class);
                });
    }

    @Test
    void populateAll_ShouldBeIdempotent()
    {
        cards.populateAll();
        cards.populateAll(); // should not duplicate entries

        List<CardDefinition> all = cards.getAll();
        assertThat(all).hasSize(52);
    }

    @Test
    void getAll_ShouldReturnOrderedBySuitAndRank()
    {
        cards.populateAll();
        List<CardDefinition> all = cards.getAll();

        // Ensure suit is sorted then rank
        for (int i = 1; i < all.size(); i++)
        {
            CardDefinition prev = all.get(i - 1);
            CardDefinition curr = all.get(i);

            if (prev.suit() == curr.suit())
                assertThat(prev.rank().toShort()).isLessThanOrEqualTo(curr.rank().toShort());
            else
                assertThat(prev.suit().toShort()).isLessThanOrEqualTo(curr.suit().toShort());
        }
    }

    @Test
    void getById_ShouldReturnMatchingCard()
    {
        cards.populateAll();
        short id = cards.getAll().getFirst().id();

        Optional<CardDefinition> found = cards.getById(id);

        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(id);
    }

    @Test
    void getById_ShouldReturnEmpty_WhenNotFound()
    {
        cards.populateAll();

        Optional<CardDefinition> missing = cards.getById((short) 999);
        assertThat(missing).isEmpty();
    }

    @Test
    void getManyById_ShouldReturnSelectedCards()
    {
        cards.populateAll();
        List<CardDefinition> all = cards.getAll();

        List<Short> ids = List.of(all.get(0).id(), all.get(1).id(), all.get(2).id());
        List<CardDefinition> subset = cards.getManyById(ids);

        assertThat(subset).hasSize(3);
        assertThat(subset).extracting(CardDefinition::id)
                .containsExactlyElementsOf(ids);
    }

    @Test
    void getManyById_ShouldReturnEmpty_WhenIdsEmpty()
    {
        cards.populateAll();
        List<CardDefinition> subset = cards.getManyById(List.of());
        assertThat(subset).isEmpty();
    }

    @Test
    void getManyById_ShouldReturnEmpty_WhenIdsNull()
    {
        cards.populateAll();
        List<CardDefinition> subset = cards.getManyById(null);
        assertThat(subset).isEmpty();
    }
}
