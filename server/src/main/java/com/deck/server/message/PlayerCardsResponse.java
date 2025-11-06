package com.deck.server.message;

import com.deck.server.message.dto.CardDTO;

import java.util.List;
import java.util.UUID;

public record PlayerCardsResponse(UUID playerId, List<CardDTO> cards) {}
