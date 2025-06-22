WITH inserted_profile_id AS (INSERT INTO profiles (
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

