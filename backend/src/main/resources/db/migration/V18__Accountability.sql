DROP TABLE IF EXISTS public.action_log;
CREATE TABLE public.action_log
(
    action_id   INTEGER     PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    type        TEXT        NOT NULL, -- stuff like 'insertion', 'deletion' etc.
    domain      TEXT        NOT NULL, -- stuff like 'building', 'location' etc.
    domain_id   INTEGER     NOT NULL, -- The ID related to the domain. e.g. if domain is building, a builing_id.
    user_id     TEXT        NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL    DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL    DEFAULT NOW(),

    CONSTRAINT fk_action_to_user
        FOREIGN KEY (user_id)
        REFERENCES public.users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

