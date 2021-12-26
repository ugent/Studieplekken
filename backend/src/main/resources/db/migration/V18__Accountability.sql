CREATE TABLE public.action_log
(
    action_id   INTEGER     PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    type        TEXT        NOT NULL,
    description TEXT        NOT NULL,
    user_id     TEXT        NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL    DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL    DEFAULT NOW(),

    CONSTRAINT fk_action_to_user
        FOREIGN KEY (user_id)
        REFERENCES public.users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

