INSERT INTO role (id, name)
VALUES
    (1, 'USER'),
    (2, 'ADMIN'),
    (3, 'STOCK_MANAGER'),
    (4, 'DRIVER'),
    (5, 'LOGISTICS');

-- DEFAULT USER ADMIN

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
    1
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

-- DEFAULT USER COMMON

INSERT INTO user_credentials (user_id, email, encrypted_password, first_name, last_name, created_at, is_active)
VALUES(
    gen_random_uuid(),
    'common@gmail.com',
    '$2a$10$LsJsFapHwz8tADcDIeXWm.lVM9tbjIJPCXhURESmA9G7Kaa1hVzbe',
    'common',
    'test',
    CURRENT_TIMESTAMP,
    TRUE
);

INSERT INTO user_profile (user_id, email, first_name, last_name, created_at, is_active)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'common@gmail.com'
        LIMIT 1
    ),
    'common@gmail.com',
    'common',
    'test',
    CURRENT_TIMESTAMP,
    TRUE
);

INSERT INTO user_role (user_id, role_id)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'common@gmail.com'
        LIMIT 1
    ),
    1
);

-- DEFAULT USER STOCK_MANAGER

INSERT INTO user_credentials (user_id, email, encrypted_password, first_name, last_name, created_at, is_active)
VALUES(
    gen_random_uuid(),
    'stockman@gmail.com',
    '$2a$10$wjRJx3ErDAjb8.qf3ZQNi.fvaiUW/dBlHGWhy58L8JHoHhj3675X.',
    'stockman',
    'test',
    CURRENT_TIMESTAMP,
    TRUE
);

INSERT INTO user_profile (user_id, email, first_name, last_name, created_at, is_active)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'stockman@gmail.com'
        LIMIT 1
    ),
    'stockman@gmail.com',
    'stockman',
    'test',
    CURRENT_TIMESTAMP,
    TRUE
);

INSERT INTO user_role (user_id, role_id)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'stockman@gmail.com'
        LIMIT 1
    ),
    3
);

INSERT INTO user_role (user_id, role_id)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'stockman@gmail.com'
        LIMIT 1
    ),
    1
);

-- DEFAULT USER DRIVER

INSERT INTO user_credentials (user_id, email, encrypted_password, first_name, last_name, created_at, is_active)
VALUES(
    gen_random_uuid(),
    'driver@gmail.com',
    '$2a$10$I46dK67EVPkrLfV2z7rBHOkIs8gv1wCGTYVbh95Hb2VYz2YprhbU.',
    'driver',
    'test',
    CURRENT_TIMESTAMP,
    TRUE
);

INSERT INTO user_profile (user_id, email, first_name, last_name, created_at, is_active)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'driver@gmail.com'
        LIMIT 1
    ),
    'driver@gmail.com',
    'driver',
    'test',
    CURRENT_TIMESTAMP,
    TRUE
);

INSERT INTO user_role (user_id, role_id)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'driver@gmail.com'
        LIMIT 1
    ),
    4
);

INSERT INTO user_role (user_id, role_id)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'driver@gmail.com'
        LIMIT 1
    ),
    1
);

-- DEFAULT USER LOGISTICS

INSERT INTO user_credentials (user_id, email, encrypted_password, first_name, last_name, created_at, is_active)
VALUES(
    gen_random_uuid(),
    'logistics@gmail.com',
    '$2a$10$i.knubAZcegWaSt.RZKfNObzg.eqx0EiO8yajq40IzH1cE7XRyNHC',
    'logistics',
    'test',
    CURRENT_TIMESTAMP,
    TRUE
);

INSERT INTO user_profile (user_id, email, first_name, last_name, created_at, is_active)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'logistics@gmail.com'
        LIMIT 1
    ),
    'logistics@gmail.com',
    'logistics',
    'test',
    CURRENT_TIMESTAMP,
    TRUE
);

INSERT INTO user_role (user_id, role_id)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'logistics@gmail.com'
        LIMIT 1
    ),
    1
);

INSERT INTO user_role (user_id, role_id)
VALUES (
    (
        SELECT u.user_id
        FROM user_credentials u
        WHERE u.email = 'logistics@gmail.com'
        LIMIT 1
    ),
    5
);