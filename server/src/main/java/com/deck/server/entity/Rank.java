package com.deck.server.entity;

public enum Rank
{
    ACE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(11),
    QUEEN(12),
    KING(13);

    private final short value;

    Rank(int value)
    {
        this.value = (short) value;
    }

    public short toShort()
    {
        return value;
    }

    public static Rank fromShort(short value)
    {
        for (Rank r : values())
        {
            if (r.value == value)
                return r;
        }
        throw new IllegalArgumentException("Invalid rank code: " + value);
    }
}
