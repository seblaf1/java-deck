package com.deck.server.entity;

import java.util.UUID;

public record GameCardEntity(UUID game_id, UUID card_id, long order_key)
{
}
