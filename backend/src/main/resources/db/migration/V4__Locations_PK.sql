-- Goal of migration:
--     - alter the public.LOCATIONS table so that the PK is an auto-increment id
--     - make sure that all tables with a FK to public.LOCATIONS use the new PK

-- Before we can drop the pk on public.LOCATIONS, we must drop the fk constraints to public.LOCATIONS.
-- These tables have a FK to public.LOCATIONS:
--     - public.LOCATION_TAGS
--     - public.CALENDAR_PERIODS
--     - public.CALENDAR_PERIODS_FOR_LOCKERS
--     - public.LOCKERS
--     - public.PENALTY_BOOK
--     - public.SCANNERS_LOCATION
alter table public.LOCATION_TAGS drop constraint fk_location_tags_to_location;
alter table public.CALENDAR_PERIODS drop constraint fk_calendar_periods_to_locations;
alter table public.CALENDAR_PERIODS_FOR_LOCKERS drop constraint fk_calendar_periods_for_lockers_to_locations;
alter table public.LOCKERS drop constraint fk_lockers_to_locations;
alter table public.PENALTY_BOOK drop constraint fk_penalty_book_to_locations;
alter table public.SCANNERS_LOCATION drop constraint fk_scanners_location_to_locations;

-- drop the current pk on public.LOCATIONS
alter table public.locations
drop constraint locations_pkey;

-- create a sequence for the id on public.LOCATIONS
create sequence if not exists locations_location_id_seq;

-- add a column which will be the new PK of public.LOCATIONS
alter table public.locations
add column location_id integer not null default nextval('locations_location_id_seq');

-- set location_id as new PK of public.LOCATIONS
alter table public.locations
add primary key (location_id);

-- add a unique constraint on the location's name
alter table public.locations
add constraint location_name_unique
unique (name);

-- tables with FK to public.LOCATIONS, these must be altered too:
--     - public.LOCATION_TAGS
--     - public.CALENDAR_PERIODS
--     - public.CALENDAR_PERIODS_FOR_LOCKERS
--     - public.LOCKERS
--     - public.PENALTY_BOOK
--     - public.SCANNERS_LOCATION
--
-- The altering strategy for each of these columns is:
--     - add the new FK column location_id
--     - initialize the new FK column for the existing records
--     - add the new foreign key constraint
--     - drop old foreign key column
--     - re-add the primary key if the old FK was part of the PK

-- Setting up public.LOCATION_TAGS
alter table public.LOCATION_TAGS
add column location_id_id integer; -- note: a column location_id already exists...

update public.LOCATION_TAGS lt
set location_id_id = l.location_id
from public.LOCATIONS l
where lt.location_id = l.name;

alter table public.LOCATION_TAGS
drop column location_id;

alter table public.LOCATION_TAGS
rename column location_id_id to location_id;

alter table public.LOCATION_TAGS
add constraint fk_location_tags_to_locations
foreign key (location_id)
    references public.LOCATIONS (location_id)
        on update cascade
        on delete cascade;

alter table public.LOCATION_TAGS
add constraint location_tags_unique
unique (location_id, tag_id);

-- Setting up public.CALENDAR_PERIODS
alter table public.CALENDAR_PERIODS
add column location_id integer;

update public.CALENDAR_PERIODS
set location_id = l.location_id
from public.LOCATIONS l
where location_name = l.name;

alter table public.CALENDAR_PERIODS
add constraint fk_calendar_periods_to_locations
foreign key (location_id)
    references public.LOCATIONS (location_id)
        on update cascade
        on delete cascade;

alter table public.CALENDAR_PERIODS
drop column location_name;

-- Setting up public.CALENDAR_PERIODS_FOR_LOCKERS
alter table public.CALENDAR_PERIODS_FOR_LOCKERS
add column location_id integer;

update public.CALENDAR_PERIODS_FOR_LOCKERS
set location_id = l.location_id
from public.LOCATIONS l
where location_name = l.name;

alter table public.CALENDAR_PERIODS_FOR_LOCKERS
add constraint fk_calendar_periods_for_lockers_to_locations
foreign key (location_id)
    references public.LOCATIONS (location_id)
        on update cascade
        on delete cascade;

alter table public.CALENDAR_PERIODS_FOR_LOCKERS
drop column location_name;

alter table public.CALENDAR_PERIODS_FOR_LOCKERS
add primary key (location_id, starts_at, ends_at, reservable_from);

-- Setting up public.LOCKER_RESERVATIONS (because this has fk to public.LOCKERS with location_name included)
alter table public.LOCKER_RESERVATIONS
drop constraint fk_locker_reservations_to_lockers;

alter table public.LOCKER_RESERVATIONS
add column location_id integer;

update public.LOCKER_RESERVATIONS
set location_id = l.location_id
from public.LOCATIONS l
where location_name = l.name;

-- assigning new FK will be done after setting up public.LOCKERS

alter table public.LOCKER_RESERVATIONS
drop column location_name;

alter table public.LOCKER_RESERVATIONS
add primary key (location_id, locker_number, user_augentid);

-- Setting up public.LOCKERS
alter table public.LOCKERS
add column location_id integer;

update public.LOCKERS
set location_id = l.location_id
from public.LOCATIONS l
where location_name = l.name;

alter table public.LOCKERS
add constraint fk_lockers_to_locations
foreign key (location_id)
    references public.LOCATIONS (location_id)
        on update cascade
        on delete cascade;

alter table public.LOCKERS
drop column location_name;

alter table public.LOCKERS
add primary key (location_id, number);

-- now, the FK of public.LOCKER_RESERVATIONS can be setup too
alter table public.LOCKER_RESERVATIONS
add constraint fk_locker_reservations_to_lockers
foreign key (location_id, locker_number)
    references public.LOCKERS (location_id, number)
        on update cascade
        on delete cascade;

-- Setting up public.PENALTY_BOOK
alter table public.PENALTY_BOOK
add column reservation_location_id integer;

update public.PENALTY_BOOK
set reservation_location_id = l.location_id
from public.LOCATIONS l
where reservation_location = l.name;

alter table public.PENALTY_BOOK
add constraint fk_penalty_book_to_locations
foreign key (reservation_location_id)
    references public.LOCATIONS (location_id)
        on update cascade
        on delete cascade;

alter table public.PENALTY_BOOK
drop column reservation_location;

-- Setting up public.SCANNERS_LOCATION
alter table public.SCANNERS_LOCATION
add column location_id integer;

update public.SCANNERS_LOCATION
set location_id = l.location_id
from public.LOCATIONS l
where location_name = l.name;

alter table public.SCANNERS_LOCATION
add constraint fk_scanners_location_to_locations
foreign key (location_id)
    references public.LOCATIONS (location_id)
        on update cascade
        on delete cascade;

alter table public.SCANNERS_LOCATION
drop column location_name;

alter table public.SCANNERS_LOCATION
add primary key (location_id, user_augentid);
