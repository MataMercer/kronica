INSERT INTO users (
    name,
    email,
    role,
    hashed_password,
    created_at
    )
    VALUES (
        'testuser',
        'testuser@gmail.com',
        'ROOT',
        'password',
         CURRENT_TIMESTAMP
         );

