CREATE TABLE user_notification_category (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE user_notification (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL,
    title       VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    category_id INT REFERENCES user_notification_category(id) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

