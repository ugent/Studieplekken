CREATE TABLE public.penalty_points
(
    penalty_id    integer primary key generated always as identity,
    user_id       text NOT NULL,
    description   text NULL,
    issuer_id     text NULL,
    class         text NOT NULL,
    points        integer NOT NULL,
    timeslot_sequence_number integer NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

        constraint fk_penalty_to_assignee
            foreign key (user_id)
                references public.users (user_id)
                on delete cascade
                on update cascade,

        constraint fk_penalty_to_assigner
            foreign key (issuer_id)
                references public.users (user_id)
                on delete cascade
                on update cascade,

        constraint fk_penalty_to_reservation
            foreign key (user_id, timeslot_sequence_number)
                references public.location_reservations (user_id, timeslot_sequence_number)
                on delete cascade
                on update cascade
);

alter table locations
ADD uses_penalty_points BOOLEAN NOT NULL default FALSE;

