package com.deck.server.entity;

import java.util.UUID;

public record DeckCardEntity(UUID id, UUID deck_id, short card_def_id)
{
}