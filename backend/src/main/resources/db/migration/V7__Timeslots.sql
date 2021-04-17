-- ALERT: this system assumes there are no calendar periods spanning multiple weeks.
-- As of the moment of speaking, this is true, and I do not expect this happening
-- This migration translates from the start-stop date system to a week-based system for calendarperiods and timeslots.


-- CALENDAR PERIODS - SETUP

-- ISO weekdate week & year
-- See https://en.wikipedia.org/wiki/ISO_week_date
alter table calendar_periods
ADD isoweek int NOT NULL default 0;

alter table calendar_periods
ADD isoyear int NOT NULL default 0;

-- Parent ID refers to the previous calendar period of this group.
-- This can be NULL if there is no previous calendar.
-- Should this parent be edited by an user, he will have the opportunity
-- to modify this calendar period as well.
alter table calendar_periods
ADD parent_id int NULL;

ALTER TABLE calendar_periods
 ADD CONSTRAINT fk_parent_id FOREIGN KEY (parent_id) REFERENCES calendar_periods(calendar_id);


-- This is the group number the calendar period belongs to.
-- This is a semantic group: all calendarperiods with the same semantics (e.g. same timeslots contained in it)
-- should belong to the same group. Should only be used for visualisation, with no other assumptions made.
alter table calendar_periods
ADD group_number int DEFAULT 0;

-- This flag specifies whether a new child calendar period should be created.
-- The scheduler will automatically detect whether it exists or not and create it.
-- Warning!: if you edit this calendar period and decide not to propagate changes,
-- you must set this flag to false, else you risk duplication.
alter table calendar_periods
add repeat_period boolean NOT NULL default false;


-- Extracting ISOWEEK and ISOYEAR according to iso week date.
-- See https://www.postgresql.org/docs/9.1/functions-datetime.html#FUNCTIONS-DATETIME-EXTRACT
-- And see also https://en.wikipedia.org/wiki/ISO_week_date
update calendar_periods
set
isoweek = EXTRACT(week from starts_at),
isoyear = EXTRACT(ISOYEAR from starts_at);




-- TIMESLOTS - SETUP

-- This is the day of week, being an int between 1 - 7.
-- See https://en.wikipedia.org/wiki/ISO_week_date.
alter table timeslots
ADD isoday_of_week SMALLINT NOT NULL default 0;


-- Multiple times a day are possible
alter table timeslots
add start_time Time NOT NULL default now();

alter table timeslots
add end_time Time NOT NULL default now();



-- Moving reservable down to the timeslot level
alter table timeslots
add reservable boolean NOT NULL default true;

update timeslots t
set 
start_time = cp.opening_time,
end_time = cp.closing_time
from calendar_periods cp
where t.calendar_id = cp.calendar_id;

-- Extracting iso day of week from date
-- See https://www.postgresql.org/docs/9.1/functions-datetime.html#FUNCTIONS-DATETIME-EXTRACT
-- And see also https://en.wikipedia.org/wiki/ISO_week_date
update timeslots
set
isoday_of_week = EXTRACT(ISODOW from timeslot_date);



-- Adding new sequence number to enable conversion for reservations
alter table timeslots
add sequence_number int NOT NULL default 0;


-- Adding unique sequence number in the entire calendar period (not just one day)
update timeslots t
set
sequence_number = counting_table.rn
from (
    -- selecting a new row number for only the calendar period of this timeslot.
select row_number() OVER (partition by t2.calendar_id ORDER BY timeslot_date DESC, timeslot_sequence_number ASC) - 1 as rn, timeslot_date, timeslot_sequence_number, cp.calendar_id as calendar_id from 
timeslots t2 inner join calendar_periods cp on t2.calendar_id  = cp.calendar_id
) as counting_table
where
t.timeslot_date = counting_table.timeslot_date and t.timeslot_sequence_number = counting_table.timeslot_sequence_number and t.calendar_id = counting_table.calendar_id;


-- Updating reservations

alter table location_reservations
add timeslot_sequence_number INT NOT NULL DEFAULT 0;

update location_reservations r
set
timeslot_sequence_number = t.sequence_number
from timeslots t
where t.timeslot_date = r.timeslot_date and t.timeslot_sequence_number = r.timeslot_seqnr and t.calendar_id = r.calendar_id;


alter table location_reservations
drop constraint fk_location_reservations_to_timeslot;

-- SETTING NEW PK CONSTRAINTS - Timeslots

alter table timeslots
drop constraint pk_timeslots;

-- Adding start_time as primary key
alter table timeslots
add constraint pk_timeslots PRIMARY KEY (calendar_id, sequence_number);

alter table location_reservations
add constraint fk_location_reservations_to_timeslot FOREIGN KEY (calendar_id, timeslot_seqnr) references timeslots(calendar_id, sequence_number);


-- CLEANUP OF NOW UNUSED COLUMNS

alter table timeslots
drop timeslot_sequence_number,
drop timeslot_date;

alter table location_reservations
drop timeslot_seqnr,
drop timeslot_date;

alter table calendar_periods
drop timeslot_length, drop reservable, drop starts_at, drop ends_at, drop opening_time, drop closing_time;
