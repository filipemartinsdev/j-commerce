CREATE TABLE refresh_token (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES user_credentials(user_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE
);

