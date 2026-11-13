CREATE TABLE files
(
    id         UUID PRIMARY KEY                             NOT NULL DEFAULT gen_random_uuid(),
    owner_id   UUID REFERENCES users (id) ON DELETE CASCADE NOT NULL,
    type       VARCHAR                                      NOT NULL,
    name       TEXT                                         NOT NULL,
    mime_type  TEXT                                         NOT NULL,
    file_size  BIGINT                                       NOT NULL,
    status     TEXT                                         NOT NULL,
    trashed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ                                  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ                                  NOT NULL DEFAULT NOW()
);