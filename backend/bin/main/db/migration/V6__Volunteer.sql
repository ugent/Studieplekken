CREATE TABLE public.roles_user_volunteer
(
    user_id      text    NOT NULL,
    location_id integer NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint fk_roles_user_volunteer_to_location
        foreign key (location_id)
            references public.locations (location_id)
            on delete cascade
            on update cascade,

    constraint fk_roles_user_location_to_user
        foreign key (user_id)
            references public.users (augentid)
            on delete cascade
            on update cascade,

    constraint uc_user_volunteer unique (user_id, location_id)
);