CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE public.users
    ADD calendar_id uuid DEFAULT (gen_random_uuid());