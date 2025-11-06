package com.deck.server.messages.dto;

import java.util.UUID;

public record PlayerValueDTO(UUID playerId, String userName, int totalValue) {}
