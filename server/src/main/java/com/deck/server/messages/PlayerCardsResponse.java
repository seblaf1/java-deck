package com.deck.server.messages;

import com.deck.server.messages.dto.CardDTO;

import java.util.List;
import java.util.UUID;

public record PlayerCardsResponse(UUID playerId, List<CardDTO> cards) {}
