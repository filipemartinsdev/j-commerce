INSERT INTO role (id, name)
VALUES
    (1, 'USER'),
    (2, 'ADMIN'),
    (3, 'STOCK_MANAGER'),
    (4, 'DRIVER');

INSERT INTO user_credentials (user_id, email, encrypted_password, first_name, last_name, created_at, is_active)
VALUES(
    gen_random_uuid(),
    'admin@gmail.com',
    '$2a$10$WhEbY/14jwqrMrzS0dIUGeb839nF9GaYbnstoWWyG1a0.xbvOgH5K',
    'admin',
    'test',
    CURRENT_TIMESTAMP,
    TRUE
);

INSERT INTO user_profile (user_id, email, first_name, last_name, created_at, is_active)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'admin@gmail.com'
        LIMIT 1
    ),
    'admin@gmail.com',
    'admin',
    'test',
    CURRENT_TIMESTAMP,
    TRUE
);

INSERT INTO user_role (user_id, role_id)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'admin@gmail.com'
        LIMIT 1
    ),
    2
);