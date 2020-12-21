CREATE TABLE users
(id UUID PRIMARY KEY,
 first_name VARCHAR(30),
 last_name VARCHAR(30),
 email VARCHAR(30) UNIQUE NOT NULL,
 admin BOOLEAN,
 last_login TIMESTAMP,
 is_active BOOLEAN NOT NULL,
 created_at TIMESTAMP NOT NULL,
 updated_at TIMESTAMP NOT NULL);
