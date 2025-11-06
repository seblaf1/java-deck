package com.deck.server.entity;

public record CardDefinition(short id, Suit suit, Rank  rank)
{
    @Override
    public String toString()
    {
        return rank.name() + " of " + suit.name();
    }
}
