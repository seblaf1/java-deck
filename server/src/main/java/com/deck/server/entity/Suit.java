package com.deck.server.entity;

public enum Suit
{
    HEARTS(0),
    SPADES(1),
    CLUBS(2),
    DIAMONDS(3);

    private final short code;

    Suit(int code)
    {
        this.code = (short) code;
    }

    public short toShort()
    {
        return code;
    }

    public static Suit fromShort(short code)
    {
        for (Suit suit : values())
        {
            if (suit.code == code)
                return suit;
        }

        throw new IllegalArgumentException("Invalid suit code: " + code);
    }
}
