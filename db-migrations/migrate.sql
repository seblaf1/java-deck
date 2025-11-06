CREATE EXTENSION IF NOT EXISTS pgcrypto;  -- for gen_random_uuid()

CREATE TABLE IF NOT EXISTS app_user (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS game (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- NOTE:
-- We provide an id to the association table so that we can reference via id instead of a composite
-- (game, user) primary key. This reduces bloating and increases performance.
CREATE TABLE IF NOT EXISTS player (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id     UUID NOT NULL REFERENCES game(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    added_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_player_per_game UNIQUE (game_id, user_id)
);


-- 52 card definitions
CREATE TABLE IF NOT EXISTS card_definition (
    id     SMALLSERIAL PRIMARY KEY,
    suit   SMALLINT NOT NULL CHECK (suit BETWEEN 0 AND 3),  -- 0=HEARTS,1=SPADES,2=CLUBS,3=DIAMONDS
    rank   SMALLINT NOT NULL CHECK (rank BETWEEN 1 AND 13), -- 1=ACE,...,13=KING
    CONSTRAINT uq_card_properties UNIQUE (suit, rank)
);


-- NOTE:
-- I'm not too sure why the specification requires to be able to create decks if all decks
-- have the same 52 cards, but there is a 'CreateDeck' API call that is required.
--
-- To support specification, we provide the 'decks' table.
CREATE TABLE IF NOT EXISTS deck (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- NOTE 1:
-- The logical reason I see for allowing deck creation is to allow deck-customization.
-- We define the deck_cards table even though our current implementation will always use the same 52,
-- making us extensible in the future.
--
-- NOTE 2:
-- We provide an id to the association table so that we can reference via id instead of a composite
-- (deck, card) primary key. This reduces bloating and increases performance. Also allows for duplicates extension.
CREATE TABLE IF NOT EXISTS deck_card (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deck_id     UUID NOT NULL REFERENCES deck(id) ON DELETE CASCADE,
    card_def_id SMALLINT NOT NULL REFERENCES card_definition(id)
    --CONSTRAINT uq_card_per_deck UNIQUE (deck_id, card_def_id) <---- removed to allow deck customization (duplicates)
);


-- === SHOE (UNDEALT CARDS ONLY) ===
CREATE TABLE IF NOT EXISTS shoe_card (
    game_id     UUID NOT NULL REFERENCES game(id) ON DELETE CASCADE,
    card_id     UUID NOT NULL REFERENCES deck_card(id) ON DELETE RESTRICT,
    order_key   BIGINT NOT NULL,  -- Fisher–Yates order for undealt cards
    CONSTRAINT pk_shoe_card PRIMARY KEY (game_id, card_id),
    CONSTRAINT uq_shoe_order UNIQUE (game_id, order_key) DEFERRABLE INITIALLY DEFERRED
);


-- === HANDS (DEALT CARDS) ===
CREATE TABLE IF NOT EXISTS hand_card (
    player_id    UUID NOT NULL REFERENCES player(id) ON DELETE RESTRICT,
    card_id      UUID NOT NULL REFERENCES deck_card(id) ON DELETE RESTRICT,
    dealt_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    hand_order   INTEGER,
    CONSTRAINT pk_hand PRIMARY KEY (player_id, card_id),
    CONSTRAINT uq_card_dealt_once UNIQUE (card_id),
    CONSTRAINT uq_hand_order UNIQUE (player_id, hand_order)
);


-- INDICES
CREATE INDEX IF NOT EXISTS ix_shoe_game_order ON shoe_card (game_id, order_key);
CREATE INDEX IF NOT EXISTS ix_deck_card_deck ON deck_card (deck_id);
CREATE INDEX IF NOT EXISTS ix_deck_card_card_def ON deck_card (card_def_id);
CREATE INDEX IF NOT EXISTS ix_shoe_game_card ON shoe_card (game_id, card_id);
CREATE INDEX IF NOT EXISTS ix_hand_player ON hand_card (player_id);
CREATE INDEX IF NOT EXISTS ix_hand_card ON hand_card (card_id);
CREATE INDEX IF NOT EXISTS ix_player_game ON player (game_id);
CREATE INDEX IF NOT EXISTS ix_player_user ON player (user_id);