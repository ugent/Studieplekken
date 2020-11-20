----------------- +------------------------------------+
----------------- |   Create application data tables   |
----------------- +------------------------------------+

CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


--
-- Name: authority; Type: TABLE; Schema: public
--

CREATE TABLE public.authority
(
    authority_id    integer primary key generated always as identity,
    authority_name  text NOT NULL unique,
    description     text NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE public.buildings
(
    building_id     integer NOT NULL primary key generated always as identity,
    building_name   text    NOT NULL unique,
    address text            NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(building_name, address)
);

--
-- Name: location; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.locations
(
    name                text    NOT NULL primary key,
    number_of_seats     integer NOT NULL,
    number_of_lockers   integer NOT NULL,
    image_url           text,
    building_id         integer NOT NULL,
    authority_id        integer NOT NULL,
    description_dutch   text,
    description_english text,
    forGroup            boolean,
    approved            boolean DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint fk_location_to_authority
        foreign key (authority_id)
            references public.authority (authority_id)
            on delete cascade
            on update cascade,

    constraint fk_location_to_building
        foreign key (building_id)
            references public.buildings (building_id)
            on delete cascade
            on update cascade
);

CREATE TABLE public.tags
(
    tag_id  integer NOT NULL primary key generated always as identity,
    dutch   text    NOT NULL unique,
    english text    NOT NULL unique,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(dutch, english)
);

CREATE TABLE public.location_tags
(
    location_id text    NOT NULL,
    tag_id      integer NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE(location_id, tag_id),

    constraint fk_location_tags_to_location
        foreign key (location_id)
            references public.locations (name)
            on delete cascade
            on update cascade,

    constraint fk_location_tags_to_tags
        foreign key (tag_id)
            references public.tags (tag_id)
            on delete cascade
            on update cascade
);

--
-- Name: calendar_periods; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.calendar_periods
(
    calendar_id     integer NOT NULL primary key generated always as identity,
    location_name   text NOT NULL,
    starts_at       Date NOT NULL,
    ends_at         Date NOT NULL,
    opening_time    Time NOT NULL,
    closing_time    Time NOT NULL,
    reservable_from Timestamp NOT NULL,
    locked_from     Timestamp NOT NULL,
    reservable      boolean NOT NULL,
    timeslot_length SMALLINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint fk_calendar_periods_to_locations
        foreign key (location_name)
            references public.locations (name)
            on delete cascade
            on update cascade
);

--
-- Name: reservation_timeslots; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reservation_timeslots
(
    calendar_id                     integer NOT NULL,
    timeslot_sequence_number        SMALLINT NOT NULL,
    timeslot_date                   Date NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint pk_timeslots
        primary key (calendar_id, timeslot_sequence_number, timeslot_date),
    constraint fk_timeslots_to_calendar_periods
        foreign key (calendar_id)
            references public.calendar_periods (calendar_id) ON DELETE CASCADE
);


--
-- Name: calendar_periods_for_lockers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.calendar_periods_for_lockers
(
    location_name   text NOT NULL,
    starts_at       Date NOT NULL,
    ends_at         Date NOT NULL,
    reservable_from Timestamp NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint pk_calendar_periods_for_lockers
        primary key (location_name, starts_at, ends_at, reservable_from),

    constraint fk_calendar_periods_for_lockers_to_locations
        foreign key (location_name)
            references public.locations (name)
            on delete cascade
            on update cascade
);

--
-- Name: institution; Type: TABLE; Schema: public
--

CREATE TABLE public.institutions
(
    name text NOT NULL primary key,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


--
-- Name: user; Type: TABLE; Schema: public
--

CREATE TABLE public.users
(
    augentid                 text NOT NULL primary key,
    augentpreferredgivenname text NOT NULL,
    augentpreferredsn        text NOT NULL,
    penalty_points           integer,
    mail                     text NOT NULL,
    password                 text,
    institution              text NOT NULL,
    admin                    boolean NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint uc_users unique (mail),

    constraint fk_users_to_institutions
        foreign key (institution)
            references public.institutions (name)
            on delete cascade
            on update cascade
);


--
-- Name: languages; Type: TABLE; Schema: public
--

CREATE TABLE public.languages
(
    enum text NOT NULL primary key,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


--
-- Name: TABLE languages; Type: COMMENT; Schema: public
--

COMMENT ON TABLE public.languages IS 'E.g. for the language ''English''
  - iso: ''en''
  - enum: ''ENGLISH''';


--
-- Name: location_reservation; Type: TABLE; Schema: public
--

CREATE TABLE public.location_reservations
(
    timeslot_date       Date NOT NULL,
    timeslot_seqnr      integer NOT NULL,
    calendar_id         integer NOT NULL,
    attended            boolean,
    user_augentid       text NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint pk_location_reservations
        primary key (timeslot_seqnr, timeslot_date, calendar_id, user_augentid),

    constraint fk_location_reservations_to_timeslot
        foreign key (timeslot_seqnr, timeslot_date, calendar_id) references public.reservation_timeslots (timeslot_sequence_number, timeslot_date, calendar_id) ON DELETE CASCADE,

    constraint fk_location_reservations_to_users
        foreign key (user_augentid)
            references public.users (augentid)
            on delete cascade
            on update cascade
);


--
-- Name: TABLE location_reservation; Type: COMMENT; Schema: public
--

COMMENT ON TABLE public.location_reservations IS 'Date in format: yyyy-MM-ddThh:mm:ss, e.g. Jan 16, 1970 at 1 PM:
1970-01-16T13:00:00';


--
-- Name: locker; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.lockers
(
    location_name text    NOT NULL,
    number        integer NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint pk_lockers
        primary key (location_name, number),

    constraint fk_lockers_to_locations
        foreign key (location_name)
            references public.locations (name)
            on delete cascade
            on update cascade
);


--
-- Name: locker_reservation; Type: TABLE; Schema: public
--

CREATE TABLE public.locker_reservations
(
    location_name   text    NOT NULL,
    locker_number   integer NOT NULL,
    user_augentid   text    NOT NULL,
    key_pickup_date Timestamp,
    key_return_date Timestamp,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint pk_locker_reservations
        primary key (location_name, locker_number, user_augentid),

    constraint fk_locker_reservations_to_lockers
        foreign key (location_name, locker_number)
            references public.lockers (location_name, number)
            on delete cascade
            on update cascade,

    constraint fk_locker_reservations_to_users
        foreign key (user_augentid)
            references public.users (augentid)
            on delete cascade
            on update cascade
);


--
-- Name: penalty_events; Type: TABLE; Schema: public
--

CREATE TABLE public.penalty_events
(
    code   integer NOT NULL primary key,
    points integer NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

--
-- Name: penalty_book; Type: TABLE; Schema: public
--

CREATE TABLE public.penalty_book
(
    user_augentid        text    NOT NULL,
    event_code           integer NOT NULL,
    "timestamp"          text    NOT NULL,
    reservation_date     text,
    received_points      integer NOT NULL,
    reservation_location text    NOT NULL,
    remarks              text,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint pk_penalty_book
        primary key (user_augentid, event_code, timestamp),

    constraint fk_penalty_book_to_penalty_events
        foreign key (event_code)
            references public.penalty_events (code)
            on delete cascade
            on update cascade,

    constraint fk_penalty_book_to_users
        foreign key (user_augentid)
            references public.users (augentid)
            on delete cascade
            on update cascade,

    constraint fk_penalty_book_to_locations
        foreign key (reservation_location)
            references public.locations (name)
            on delete cascade
            on update cascade
);


--
-- Name: TABLE penalty_book; Type: COMMENT; Schema: public
--

COMMENT ON TABLE public.penalty_book IS 'This table contains all penalties (like an order book in the stock exchange, containing all orders): links a penalty_events record to a user record
Note: if points is NULL, the points field from penalty_events record indicates the amount of points that are given to a user.';


--
-- Name: penalty_descriptions; Type: TABLE; Schema: public
--

CREATE TABLE public.penalty_descriptions
(
    lang_enum   text    NOT NULL,
    event_code  integer NOT NULL,
    description text    NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint pk_penalty_descriptions
        primary key (lang_enum, event_code),

    constraint fk_penalty_descriptions_to_languages
        foreign key (lang_enum)
            references languages (enum)
            on delete cascade
            on update cascade,

    constraint fk_penalty_descriptions_to_penalty_events
        foreign key (event_code)
            references penalty_events (code)
            on delete cascade
            on update cascade
);

--
-- Name: TABLE penalty_events; Type: COMMENT; Schema: public
--

COMMENT ON TABLE public.penalty_events IS 'Some remarks:
(1) codes that start with 1666 are special codes: these represent events which are not deletable. For example: when a student cancels a reservation too late, the amount of points are based on how much he/she cancels too late (the amount grows linearly in function of the time, until a max has reached (i.e. the value at ''points'')).
(2) the field public_accessible is meant for whether a student can see this event in an overview or not (so that the student knows for which penalties he/she will receive what amount of points). Some PenaltyEvents may not be used currently, so that the event shouldn''t be in the overview. Therefore this field has been provided.';


--
-- Name: scanners_location; Type: TABLE; Schema: public
--

CREATE TABLE public.scanners_location
(
    location_name text NOT NULL,
    user_augentid text NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint pk_scanners_location
        primary key (location_name, user_augentid),

    constraint fd_scanners_location_to_users
        foreign key (user_augentid)
            references public.users (augentid)
            on delete cascade
            on update cascade,

    constraint fk_scanners_location_to_locations
        foreign key (location_name)
            references public.locations (name)
            on delete cascade
            on update cascade
);


--
-- Name: TABLE penalty_book; Type: COMMENT; Schema: public
--

COMMENT ON TABLE public.users IS 'Be aware: the column penalty_points does not mean anything as a value. Because every time a USER is selected, the penalty points need to be calculated using the PENALTY_BOOK';

--
-- Name: users_to_verify; Type: TABLE; Schema: public
--

CREATE TABLE public.users_to_verify
(
    mail                     text NOT NULL,
    augentpreferredsn        text NOT NULL,
    augentpreferredgivenname text NOT NULL,
    password                 text NOT NULL,
    institution              text NOT NULL,
    augentid                 text NOT NULL primary key,
    verification_code        text NOT NULL,
    created_timestamp        text NOT NULL,
    admin                    boolean NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint uc_users_to_verify unique (mail),

    constraint fk_users_to_verify_to_institutions
        foreign key (institution)
            references public.institutions (name)
            on delete cascade
            on update cascade
);

--
-- Name: roles_user_authority; Type: TABLE; Schema: public
--

CREATE TABLE public.roles_user_authority
(
    user_id      text    NOT NULL,
    authority_id integer NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    constraint fk_roles_user_authority_to_authority
        foreign key (authority_id)
            references public.authority (authority_id)
            on delete cascade
            on update cascade,

    constraint fk_roles_user_authority_to_user
        foreign key (user_id)
            references public.users (augentid)
            on delete cascade
            on update cascade,

    constraint uc_user_authority unique (user_id, authority_id)
);

----------------- +------------------+
----------------- |   Add triggers   |
----------------- +------------------+

CREATE TRIGGER set_timestamp_user_authority
BEFORE UPDATE ON public.roles_user_authority
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_users_to_verify
BEFORE UPDATE ON public.users_to_verify
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_scanners_location
BEFORE UPDATE ON public.scanners_location
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_penalty_descriptions
BEFORE UPDATE ON public.penalty_descriptions
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_penalty_book
BEFORE UPDATE ON public.penalty_book
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_penalty_events
BEFORE UPDATE ON public.penalty_events
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_locker_reservations
BEFORE UPDATE ON public.locker_reservations
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_lockers
BEFORE UPDATE ON public.lockers
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_location_reservations
BEFORE UPDATE ON public.location_reservations
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_languages
BEFORE UPDATE ON public.languages
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_users
BEFORE UPDATE ON public.users
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_institutions
BEFORE UPDATE ON public.institutions
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_calendar_periods_for_lockers
BEFORE UPDATE ON public.calendar_periods_for_lockers
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_reservation_timeslots
BEFORE UPDATE ON public.reservation_timeslots
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_calendar_periods
BEFORE UPDATE ON public.calendar_periods
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_location_tags
BEFORE UPDATE ON public.location_tags
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_tags
BEFORE UPDATE ON public.tags
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_locations
BEFORE UPDATE ON public.locations
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_buildings
BEFORE UPDATE ON public.buildings
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_authority
BEFORE UPDATE ON public.authority
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

----------------- +-----------------------+
----------------- |   Import extensions   |
----------------- +-----------------------+
-- create extension fuzzystrmatch;

----------------- +-------------------------------------------+
----------------- |   Insert application data into database   |
----------------- +-------------------------------------------+

--
-- Data for table: institution
--
insert into public.institutions (name)
values ('UGent'),
       ('HoGent'),
       ('Artevelde Hogeschool');

--
-- Data for table: languages
--
insert into public.languages (enum)
values ('ENGLISH'),
       ('DUTCH');

--
-- Data for table: penalty_events
--
insert into public.penalty_events (code, points)
values (16660, 30),
       (16661, 50),
       (16662, 100),
       (16663, 0);

--
-- Data for table: penalty_descriptions
--
insert into public.penalty_descriptions (lang_enum, event_code, description)
values ('ENGLISH', 16660, 'Cancelling too late.'),
       ('DUTCH', 16660, 'Te laat annuleren.'),
       ('ENGLISH', 16661, 'Not showing up at all.'),
       ('DUTCH', 16661, 'Niet komen opdagen.'),
       ('ENGLISH', 16662, 'Blacklist event.'),
       ('DUTCH', 16662, 'Blacklist event.'),
       ('ENGLISH', 16663, 'Manual entry.'),
       ('DUTCH', 16663, 'Manual entry.');
