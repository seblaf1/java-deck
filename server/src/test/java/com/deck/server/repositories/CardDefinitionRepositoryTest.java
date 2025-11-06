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
class CardDefinitionRepositoryTest {

    @Autowired private CardDefinitionRepository cards;

    @Test
    void populateAndReadCards() {
        cards.populateAll();
        List<Map<String, Object>> all = cards.getAll();

        assertThat(all).hasSize(52);
        assertThat(all.getFirst()).containsKeys("id", "suit", "rank");
    }

    @Test
    void getByIdWorks() {
        cards.populateAll();
        short firstId = ((Number) cards.getAll().getFirst().get("id")).shortValue();

        Optional<Map<String, Object>> found = cards.getById(firstId);
        assertThat(found).isPresent();
        assertThat(((Number) found.get().get("id")).intValue())
                .isEqualTo(firstId);
    }
}
