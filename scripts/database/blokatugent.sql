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

----------------- +------------------------------------+
----------------- |   Create application data tables   |
----------------- +------------------------------------+

--
-- Name: calendar; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.calendar (
    location_name text NOT NULL,
    opening_time time without time zone NOT NULL,
    closing_time time without time zone NOT NULL,
    open_for_reservation_date text NOT NULL,
    date text NOT NULL
);


ALTER TABLE public.calendar OWNER TO postgres;

--
-- Name: institution; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institutions (
    name text NOT NULL
);


ALTER TABLE public.institutions OWNER TO postgres;

--
-- Name: languages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.languages (
    enum text NOT NULL
);


ALTER TABLE public.languages OWNER TO postgres;

--
-- Name: TABLE languages; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.languages IS 'E.g. for the language ''English''
  - iso: ''en''
  - enum: ''ENGLISH''';


--
-- Name: location; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.locations (
    name text NOT NULL,
    number_of_seats integer NOT NULL,
    number_of_lockers text NOT NULL,
    maps_frame text NOT NULL,
    image_url text,
    address text NOT NULL,
    start_period_lockers text,
    end_period_lockers text
);


ALTER TABLE public.locations OWNER TO postgres;

--
-- Name: location_descriptions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.location_descriptions (
    location_name text NOT NULL,
    lang_enum text NOT NULL,
    description text NOT NULL
);


ALTER TABLE public.location_descriptions OWNER TO postgres;

--
-- Name: location_reservation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.location_reservations (
    date text NOT NULL,
    location_name text NOT NULL,
    attended boolean,
    user_augentid text
);


ALTER TABLE public.location_reservations OWNER TO postgres;

--
-- Name: TABLE location_reservation; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.location_reservations IS 'Date in format: yyyy-MM-ddThh:mm:ss, e.g. Jan 16, 1970 at 1 PM:
1970-01-16T13:00:00';


--
-- Name: locker; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.lockers (
    location_name text NOT NULL,
    number integer NOT NULL,
    id serial NOT NULL
);


ALTER TABLE public.lockers OWNER TO postgres;

--
-- Name: locker_reservation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.locker_reservations (
    start_date text NOT NULL,
    locker_id integer NOT NULL,
    end_date text NOT NULL,
    user_augentid text,
    key_picked_up boolean NOT NULL,
    key_brought_back boolean NOT NULL
);


ALTER TABLE public.locker_reservations OWNER TO postgres;

--
-- Name: penalty_book; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.penalty_book (
    user_augentid text NOT NULL,
    event_code integer NOT NULL,
    "timestamp" text NOT NULL,
    reservation_date text NOT NULL,
    received_points integer NOT NULL,
    reservation_location text NOT NULL
);


ALTER TABLE public.penalty_book OWNER TO postgres;

--
-- Name: TABLE penalty_book; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.penalty_book IS 'This table contains all penalties (like an order book in the stock exchange, containing all orders): links a penalty_events record to a user record
Note: if points is NULL, the points field from penalty_events record indicates the amount of points that are given to a user.';


--
-- Name: penalty_descriptions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.penalty_descriptions (
    lang_enum text NOT NULL,
    event_code integer NOT NULL,
    description text NOT NULL
);


ALTER TABLE public.penalty_descriptions OWNER TO postgres;

--
-- Name: penalty_events; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.penalty_events (
    code integer NOT NULL,
    points integer NOT NULL,
    public_accessible boolean NOT NULL
);


ALTER TABLE public.penalty_events OWNER TO postgres;

--
-- Name: TABLE penalty_events; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.penalty_events IS 'Some remarks:
(1) codes that start with 1666 are special codes: these represent events which are not deletable. For example: when a student cancels a reservation too late, the amount of points are based on how much he/she cancels too late (the amount grows linearly in function of the time, until a max has reached (i.e. the value at ''points'')).
(2) the field public_accessible is meant for whether a student can see this event in an overview or not (so that the student knows for which penalties he/she will receive what amount of points). Some PenaltyEvents may not be used currently, so that the event shouldn''t be in the overview. Therefore this field has been provided.';

--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    type text NOT NULL
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: scanners_location; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.scanners_location (
    location_name text NOT NULL,
    user_augentid text NOT NULL
);


ALTER TABLE public.scanners_location OWNER TO postgres;

--
-- Name: user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    augentid text NOT NULL,
    role text NOT NULL,
    augentpreferredgivenname text NOT NULL,
    augentpreferredsn text NOT NULL,
    penalty_points integer,
    mail text NOT NULL,
    password text,
    institution text NOT NULL,
    barcode text
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_to_verify; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users_to_verify (
    mail text NOT NULL,
    augentpreferredsn text NOT NULL,
    augentpreferredgivenname text NOT NULL,
    password text NOT NULL,
    institution text NOT NULL,
    augentid text NOT NULL,
    role text NOT NULL,
    barcode text,
    verification_code text NOT NULL,
    created_timestamp text NOT NULL
);


ALTER TABLE public.users_to_verify OWNER TO postgres;










----------------- +----------------------+
----------------- |   Set primary keys   |
----------------- +----------------------+
alter table only public.calendar
add constraint pk_calendar 
primary key (
	location_name
	, opening_time
	, closing_time
	, date
);

alter table only public.institutions
add constraint pk_institutions 
primary key (
	name
);

alter table only public.languages
add constraint pk_languages
primary key (
	enum
);

alter table only public.locations
add constraint pk_locations 
primary key (
	name 
);

alter table only public.location_descriptions
add constraint pk_location_descriptions 
primary key (
	location_name
	, lang_enum
);

alter table only public.location_reservations
add constraint pk_location_reservations
primary key (
	date
	, location_name
	, user_augentid
);

alter table only public.lockers
add constraint pk_lockers 
primary key (
	id
);

alter table only public.locker_reservations
add constraint pk_locker_reservations 
primary key (
	start_date
	, locker_id
	, end_date
	, user_augentid
);

alter table only public.penalty_book
add constraint pk_penalty_book 
primary key (
	user_augentid
	, event_code
	, timestamp
);

alter table only public.penalty_descriptions
add constraint pk_penalty_descriptions 
primary key (
	lang_enum
	, event_code
);

alter table only public.penalty_events
add constraint pk_penalty_events 
primary key (
	code
);

alter table only public.roles
add constraint pk_roles 
primary key (
	type
);

alter table only public.scanners_location
add constraint pk_scanners_location 
primary key (
	location_name
	, user_augentid
);

alter table only public.users
add constraint pk_users 
primary key (
	augentid
);

alter table only public.users_to_verify
add constraint pk_users_to_verify 
primary key (
	augentid
);










----------------- +----------------------+
----------------- |   Set foreign keys   |
----------------- +----------------------+

--
-- calendar to locations
--
alter table only public.calendar
add constraint fk_calendar_to_locations
foreign key (location_name)
references public.locations (name);

--
-- location_descriptions to locations, languages
--
alter table only public.location_descriptions
add constraint fk_location_descriptions_to_locations
foreign key (location_name)
references public.locations (name);

alter table only public.location_descriptions
add constraint fk_location_descriptions_to_languages
foreign key (lang_enum)
references public.languages (enum);

--
-- location_reservations to locations, users
--	
alter table only public.location_reservations
add constraint fk_location_reservations_to_location
foreign key (location_name)
references public.locations (name);

alter table only public.location_reservations
add constraint fk_location_reservations_to_users
foreign key (user_augentid)
references public.users (augentid);

--
-- lockers to locations
--
alter table only public.lockers
add constraint fk_lockers_to_locations
foreign key (location_name)
references public.locations (name);

--
-- locker_reservations to lockers, users 
--
alter table only public.locker_reservations
add constraint fk_locker_reservations_to_lockers
foreign key (locker_id)
references public.lockers (id);

alter table only public.locker_reservations
add constraint fk_locker_reservations_to_users
foreign key (user_augentid)
references public.users (augentid);

--
-- penalty_book to users, locations, penalty_events
--
alter table only public.penalty_book
add constraint fk_penalty_book_to_users
foreign key (user_augentid)
references public.users (augentid);

alter table only public.penalty_book
add constraint fk_penalty_book_to_locations
foreign key (reservation_location)
references public.locations (name);

alter table only public.penalty_book
add constraint fk_penalty_book_to_penalty_events
foreign key (event_code)
references public.penalty_events (code);

--
-- penalty_descriptions to penatly_events, languages
--
alter table only public.penalty_descriptions
add constraint fk_penalty_descriptions_to_penalty_events
foreign key (event_code)
references public.penalty_events (code);

alter table only public.penalty_descriptions
add constraint fk_penalty_descriptions_to_languages
foreign key (lang_enum)
references public.languages (enum);

--
-- scanners_location to locations, users
--
alter table only public.scanners_location
add constraint fk_scanners_location_to_locations
foreign key (location_name)
references public.locations (name);

alter table only public.scanners_location
add constraint fd_scanners_location_to_users
foreign key (user_augentid)
references public.users (augentid);

--
-- users to institutions
--
alter table only public.users
add constraint fk_users_to_institutions
foreign key (institution)
references public.institutions (name);


--
-- users_to_verify to institutions
--
alter table only public.users_to_verify
add constraint fk_users_to_verify_to_institutions
foreign key (institution)
references public.institutions (name);









----------------- +----------------------------+
----------------- |   Set unique constraints   |
----------------- +----------------------------+
alter table public.lockers
add constraint uc_lockers
unique (location_name, number);

alter table public.users 
add constraint uc_users
unique (mail);

alter table public.users_to_verify
add constraint uc_users_to_verify
unique (mail);










----------------- +-------------------------------------------+
----------------- |   Insert application data into database   |
----------------- +-------------------------------------------+

--
-- Data for table: institution
--
insert into public.institutions (name)
values ('UGent'), ('HoGent'), ('Artevelde Hogeschool');

--
-- Data for table: languages
--
insert into public.languages (enum)
values ('ENGLISH'), ('DUTCH');

--
-- Data for table: penalty_events 
--
insert into public.penalty_events (code, points, public_accessible)
values (16660, 30, true), (16661, 50, true), (16662, 100, true);

--
-- Data for table: penalty_descriptions
--
insert into public.penalty_descriptions (lang_enum, event_code, description)
values ('ENGLISH', 16660, 'Cancelling too late.'),
('DUTCH', 16660, 'Te laat annuleren.'),
('ENGLISH', 16661, 'Not showing up at all.'),
('DUTCH', 16661, 'Niet komen opdagen.'),
('ENGLISH', 16662, 'Blacklist event.'),
('DUTCH', 16662, 'Blacklist event.');

--
-- Data for table: roles 
--
insert into public.roles (type)
values ('ADMIN'), ('EMPLOYEE'), ('STUDENT');

--
-- Dummy user accounts:
--		- user: admin 						password: Test1234
--		- user: scanmedewerker 				password: Test1234
--		- user: student-scanmedewerker		password: Test1234
--
insert into public.users (augentid, role, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password, institution, barcode)
values ('01', 'ADMIN;EMPLOYEE', 'admin', 'admin', 0, 'admin', '$2a$10$3sAUdBwt2sJhhMl3MVZAv.Pv56XHGo2kTbyS.nFeMmPb3dHA1kkTq', 'UGent', '001703195697'),
('02', 'EMPLOYEE', 'scanmedewerker', 'scanmedewerker', 0, 'scanmedewerker', '$2a$10$3sAUdBwt2sJhhMl3MVZAv.Pv56XHGo2kTbyS.nFeMmPb3dHA1kkTq', 'UGent', '001703195697'),
('03', 'STUDENT;EMPLOYEE', 'student-scanmedewerker', 'student-scanmedewerker', 0, 'student-scanmedewerker', '$2a$10$3sAUdBwt2sJhhMl3MVZAv.Pv56XHGo2kTbyS.nFeMmPb3dHA1kkTq', 'UGent', '001703195697');