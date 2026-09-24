CREATE TABLE note
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version    BIGINT       NOT NULL,
    title      VARCHAR(200) NOT NULL,
    content    TEXT,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL
);

CREATE INDEX note_updated_at_idx ON note (updated_at DESC, id DESC);
