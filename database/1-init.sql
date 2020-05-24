-- This keeps track of when entities are updated.
CREATE OR REPLACE FUNCTION update_timestamps() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  NEW.created_at = OLD.created_at;
  RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- Simple users table.
CREATE TABLE users (
  id SERIAL PRIMARY KEY NOT NULL,
  username TEXT,
  phone_number TEXT,
  secret TEXT NOT NULL,
  pw_hash TEXT,
  salt TEXT,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  UNIQUE(username),
  UNIQUE(phone_number)
);

CREATE TRIGGER users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE PROCEDURE update_timestamps();

-- Events table.
CREATE TABLE events (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  user_id INTEGER NOT NULL,
  domain TEXT NOT NULL,
  path TEXT NOT NULL,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TRIGGER events_updated_at
BEFORE UPDATE ON events
FOR EACH ROW
EXECUTE PROCEDURE update_timestamps();
