package com.deck.server.dto;

import com.deck.server.entity.CardDefinition;

public record CardDto(int suit, int rank)
{
    public static CardDto fromDefinition(CardDefinition def)
    {
        return new CardDto(def.suit().ordinal(), def.rank().ordinal());
    }
}
