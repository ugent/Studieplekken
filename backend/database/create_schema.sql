--
-- IMPORTANT: The database should be created.
-- In PgAdmin: create database and open query tool, copy this script and run.
--

/* -- With pgAdmin, the following SQL code will be executed:
CREATE DATABASE blokatugent
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;
*/

drop schema IF EXISTS public cascade;

CREATE SCHEMA public
    AUTHORIZATION postgres;

----------------- +------------------------------------+
----------------- |   Create application data tables   |
----------------- +------------------------------------+


--
-- Name: authority; Type: TABLE; Schema: public
--

CREATE TABLE public.authority
(
    authority_id integer primary key generated always as identity,
    name         text NOT NULL unique,
    description  text NOT NULL
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
    address             text    NOT NULL,
    authority_id        integer NOT NULL,
    description_dutch   text,
    description_english text,

    constraint fk_location_to_authority
        foreign key (authority_id)
            references public.authority (authority_id)
);

CREATE TABLE public.tags
(
    tag_id  integer NOT NULL primary key,
    dutch   text    NOT NULL unique,
    english text    NOT NULL unique
);

CREATE TABLE public.location_tags
(
    location_id text NOT NULL,
    tag_id integer NOT NULL,
    constraint fk_location_tags_to_location
        foreign key (location_id) references public.locations (name),
    constraint fk_location_tags_to_tags
        foreign key (tag_id) references public.tags (tag_id)
);

--
-- Name: calendar_periods; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.calendar_periods
(
    location_name   text NOT NULL,
    starts_at       text NOT NULL,
    ends_at         text NOT NULL,
    opening_time    text NOT NULL,
    closing_time    text NOT NULL,
    reservable_from text NOT NULL,

    constraint pk_calendar_periods
        primary key (
                     location_name, starts_at, ends_at, opening_time, closing_time, reservable_from
            ),
    constraint fk_calendar_periods_to_locations
        foreign key (location_name)
            references public.locations (name)
);

--
-- Name: calendar_periods_for_lockers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.calendar_periods_for_lockers
(
    location_name   text NOT NULL,
    starts_at       text NOT NULL,
    ends_at         text NOT NULL,
    reservable_from text NOT NULL,

    constraint pk_calendar_periods_for_lockers
        primary key (
                     location_name, starts_at, ends_at, reservable_from
            ),
    constraint fk_calendar_periods_for_lockers_to_locations
        foreign key (location_name)
            references public.locations (name)
);

--
-- Name: institution; Type: TABLE; Schema: public
--

CREATE TABLE public.institutions
(
    name text NOT NULL primary key
);


--
-- Name: user; Type: TABLE; Schema: public
--

CREATE TABLE public.users
(
    augentid                 text NOT NULL primary key,
    role                     text NOT NULL,
    augentpreferredgivenname text NOT NULL,
    augentpreferredsn        text NOT NULL,
    penalty_points           integer,
    mail                     text NOT NULL,
    password                 text,
    institution              text NOT NULL,

    constraint uc_users unique (mail),

    constraint fk_users_to_institutions
        foreign key (institution) references public.institutions (name)
);


--
-- Name: languages; Type: TABLE; Schema: public
--

CREATE TABLE public.languages
(
    enum text NOT NULL primary key
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
    date          text NOT NULL,
    location_name text NOT NULL,
    attended      boolean,
    user_augentid text NOT NULL,

    constraint pk_location_reservations
        primary key (date, user_augentid),

    constraint fk_location_reservations_to_location
        foreign key (location_name) references public.locations (name),
    constraint fk_location_reservations_to_users
        foreign key (user_augentid) references public.users (augentid)
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

    constraint pk_lockers
        primary key (location_name, number),

    constraint fk_lockers_to_locations
        foreign key (location_name) references public.locations (name)
);


--
-- Name: locker_reservation; Type: TABLE; Schema: public
--

CREATE TABLE public.locker_reservations
(
    location_name   text    NOT NULL,
    locker_number   integer NOT NULL,
    user_augentid   text    NOT NULL,
    key_pickup_date text,
    key_return_date text,

    constraint pk_locker_reservations
        primary key (location_name, locker_number, user_augentid),

    constraint fk_locker_reservations_to_lockers
        foreign key (location_name, locker_number) references public.lockers (location_name, number),
    constraint fk_locker_reservations_to_users
        foreign key (user_augentid) references public.users (augentid)
);


--
-- Name: penalty_events; Type: TABLE; Schema: public
--

CREATE TABLE public.penalty_events
(
    code   integer NOT NULL primary key,
    points integer NOT NULL
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

    constraint pk_penalty_book
        primary key (user_augentid, event_code, timestamp),

    constraint fk_penalty_book_to_penalty_events
        foreign key (event_code) references public.penalty_events (code),
    constraint fk_penalty_book_to_users
        foreign key (user_augentid) references public.users (augentid),
    constraint fk_penalty_book_to_locations
        foreign key (reservation_location) references public.locations (name)
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

    constraint pk_penalty_descriptions
        primary key (lang_enum, event_code),

    constraint fk_penalty_descriptions_to_languages
        foreign key (lang_enum) references languages (enum),
    constraint fk_penalty_descriptions_to_penalty_events
        foreign key (event_code) references penalty_events (code)
);

--
-- Name: TABLE penalty_events; Type: COMMENT; Schema: public
--

COMMENT ON TABLE public.penalty_events IS 'Some remarks:
(1) codes that start with 1666 are special codes: these represent events which are not deletable. For example: when a student cancels a reservation too late, the amount of points are based on how much he/she cancels too late (the amount grows linearly in function of the time, until a max has reached (i.e. the value at ''points'')).
(2) the field public_accessible is meant for whether a student can see this event in an overview or not (so that the student knows for which penalties he/she will receive what amount of points). Some PenaltyEvents may not be used currently, so that the event shouldn''t be in the overview. Therefore this field has been provided.';

--
-- Name: roles; Type: TABLE; Schema: public
--

CREATE TABLE public.roles
(
    type text NOT NULL primary key
);


--
-- Name: scanners_location; Type: TABLE; Schema: public
--

CREATE TABLE public.scanners_location
(
    location_name text NOT NULL,
    user_augentid text NOT NULL,

    constraint pk_scanners_location
        primary key (location_name, user_augentid),

    constraint fd_scanners_location_to_users
        foreign key (user_augentid) references public.users (augentid),
    constraint fk_scanners_location_to_locations
        foreign key (location_name) references public.locations (name)
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
    role                     text NOT NULL,
    verification_code        text NOT NULL,
    created_timestamp        text NOT NULL,

    constraint uc_users_to_verify unique (mail),

    constraint fk_users_to_verify_to_institutions
        foreign key (institution) references public.institutions (name)
);

--
-- Name: roles_user_authority; Type: TABLE; Schema: public
--

CREATE TABLE public.roles_user_authority
(
    user_id      text    NOT NULL,
    authority_id integer NOT NULL,

    constraint fk_roles_user_authority_to_authority
        foreign key (authority_id) references public.authority (authority_id),
    constraint fk_roles_user_authority_to_user
        foreign key (user_id) references public.users (augentid)
);

----------------- +-----------------------+
----------------- |   Import extensions   |
----------------- +-----------------------+
create extension fuzzystrmatch;

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

--
-- Data for table: roles
--
insert into public.roles (type)
values ('ADMIN'),
       ('EMPLOYEE'),
       ('STUDENT');

--
-- Dummy user accounts:
--		- user: admin 						password: Test1234
--		- user: scanmedewerker 				password: Test1234
--		- user: student-scanmedewerker		password: Test1234
--
insert into public.users (augentid, role, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password,
                          institution)
values ('01', 'ADMIN$EMPLOYEE', 'admin', 'admin', 0, 'admin',
        '$2a$10$3sAUdBwt2sJhhMl3MVZAv.Pv56XHGo2kTbyS.nFeMmPb3dHA1kkTq', 'UGent'),
       ('02', 'EMPLOYEE', 'scanmedewerker', 'scanmedewerker', 0, 'scanmedewerker',
        '$2a$10$3sAUdBwt2sJhhMl3MVZAv.Pv56XHGo2kTbyS.nFeMmPb3dHA1kkTq', 'UGent'),
       ('03', 'STUDENT$EMPLOYEE', 'student-scanmedewerker', 'student-scanmedewerker', 0, 'student-scanmedewerker',
        '$2a$10$3sAUdBwt2sJhhMl3MVZAv.Pv56XHGo2kTbyS.nFeMmPb3dHA1kkTq', 'UGent');
