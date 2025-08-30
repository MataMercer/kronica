WITH inserted_profile_id AS (INSERT INTO user_profiles (
    description
    )
    VALUES (
        'testuser profile'
        ) RETURNING id)

INSERT INTO users (
    name,
    email,
    role,
    hashed_password,
    created_at,
    profile_id,
    auth_provider
    )
    VALUES (
        'testuser',
        'testuser@gmail.com',
        'ROOT',
        'password',
         CURRENT_TIMESTAMP,
         (SELECT id FROM inserted_profile_id),
        'LOCAL'
         );

WITH inserted_content_id AS (INSERT INTO content (
    author_id,
    created_at,
    updated_at
)
VALUES(
 (SELECT id FROM users WHERE name = 'testuser'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
 ) RETURNING id)

INSERT INTO articles
    (
    id,
    title,
    body
    )
VALUES (
    (SELECT id FROM inserted_content_id),
    'Example Title',
    'Example Body Lorem ipsum...'
    );

