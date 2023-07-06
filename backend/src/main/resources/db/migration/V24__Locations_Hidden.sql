DROP TABLE IF EXISTS public.user_location_subscription;
CREATE TABLE public.user_location_subscription
(
    subscription_id            integer primary key generated always as identity,
    user_id                    text NOT NULL,
    location_id                integer NOT NULL,
    CONSTRAINT fk_user_location_subscription_user
        FOREIGN KEY(user_id)
            REFERENCES public.users(user_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT fk_user_location_subscription_location
        FOREIGN KEY(location_id)
            REFERENCES public.locations(location_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);