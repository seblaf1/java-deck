package com.deck.server.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record User(
        UUID id,
        String name,
        OffsetDateTime createdAt)
{
}
