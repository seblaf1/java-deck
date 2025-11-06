package com.deck.server.message.dto;

import java.util.UUID;

public record PlayerValueDTO(UUID playerId, String userName, int totalValue) {}
