CREATE TABLE user_credentials (
    user_id             UUID PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    encrypted_password  VARCHAR(255) NOT NULL,
    first_name          VARCHAR(50) NOT NULL,
    last_name           VARCHAR(255) NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_profile (
    user_id     UUID PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    first_name  VARCHAR(50) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE refresh_token (
    id          UUID PRIMARY KEY,
    user_id     UUID REFERENCES user_credentials(user_id),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked  BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE role (
    id      INTEGER PRIMARY KEY,
    name    VARCHAR(255) NOT NULL
);

CREATE TABLE user_role (
    user_id     UUID REFERENCES user_credentials(user_id),
    role_id     INTEGER REFERENCES role(id),
    PRIMARY KEY (user_id, role_id)
);