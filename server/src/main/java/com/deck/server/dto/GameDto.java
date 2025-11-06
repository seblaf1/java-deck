package com.deck.server.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GameDto(UUID id, OffsetDateTime createdAt) { }
