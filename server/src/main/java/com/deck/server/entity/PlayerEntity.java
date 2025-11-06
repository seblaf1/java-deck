package com.deck.server.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PlayerEntity(
        UUID id,
        UUID gameId,
        UUID userId,
        String userName,
        OffsetDateTime addedAt
)
{
}
