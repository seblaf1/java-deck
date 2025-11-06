package com.deck.server.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GameEntity(UUID id, OffsetDateTime createdAt)
{
}
