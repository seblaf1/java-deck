package com.deck.server.dto;

import java.util.UUID;

public record PlayerDto(UUID playerId, String playerName, int totalValue)
{
}
