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

INSERT INTO articles
    (title,
    body,
    created_at,
    updated_at,
    author_id
    )
VALUES (
    'Example Title',
    'Example Body Lorem ipsum...',
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP,
    (SELECT id FROM users WHERE name = 'testuser')
    );

