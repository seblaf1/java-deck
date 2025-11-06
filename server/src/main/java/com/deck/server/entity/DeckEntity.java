package com.deck.server.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeckEntity(
        UUID id,
        String name,
        OffsetDateTime createdAt)
{
}