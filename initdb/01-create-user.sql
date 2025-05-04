-- Must use IF NOT EXISTS to be idempotent
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'karaoke_user') THEN
        CREATE ROLE karaoke_user WITH LOGIN PASSWORD 'karaoke_pass';
    END IF;
END
$$;

CREATE SCHEMA IF NOT EXISTS karaoke_schema AUTHORIZATION karaoke_user;
GRANT ALL PRIVILEGES ON SCHEMA karaoke_schema TO karaoke_user;