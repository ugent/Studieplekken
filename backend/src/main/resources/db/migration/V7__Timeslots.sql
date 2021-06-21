-- ALERT: this system assumes there are no calendar periods spanning multiple weeks.
-- As of the moment of speaking, this is true, and I do not expect this happening
-- This migration translates from the calendarperiod based system to an independently stored timeslot implementation.



-- TIMESLOTS moving down Calendarperiod info
alter table timeslots
ADD isoday_of_week SMALLINT NOT NULL default 0;

alter table timeslots
ADD iso_week SMALLINT NOT NULL default 0;

alter table timeslots
ADD iso_year SMALLINT NOT NULL default 0;

alter table timeslots
ADD reservable boolean NOT NULL default true;

alter table timeslots
ADD opening_hour Time NOT NULL default now();

alter table timeslots
ADD closing_hour Time NOT NULL default now();

CREATE SEQUENCE if not exists timeslot_id_seq;
SELECT setval('timeslot_id_seq',  (SELECT count(*)+1 FROM timeslots));
alter table timeslots
add sequence_number int NOT NULL default nextval('timeslot_id_seq');

alter table timeslots
add timeslot_group int;

alter table timeslots
add reservable_from timestamp;

alter table timeslots
add location_id int;


-- TIMESLOTS filling in new data

-- setting iso_week, iso_year, iso_day_of_week
update timeslots
set
isoday_of_week = EXTRACT(ISODOW from timeslot_date),
iso_week = EXTRACT(week from timeslot_date),
iso_year = EXTRACT(ISOYEAR from timeslot_date);


-- setting reservable_from and reservable
update timeslots
set
reservable_from = cp.reservable_from,
reservable = cp.reservable,
location_id = cp.location_id
from calendar_periods cp
where cp.calendar_id = timeslots.calendar_id;

-- setting opening, closing hour
update timeslots
set
opening_hour = (cp.timeslot_length * timeslot_sequence_number * interval '1 minute') + cp.opening_time,
closing_hour =(cp.timeslot_length * (timeslot_sequence_number + 1) * interval '1 minute') + cp.opening_time
from calendar_periods cp
where cp.calendar_id = timeslots.calendar_id;

-- Adding unique sequence number in the entire calendar period (not just one day)
update timeslots t
set
sequence_number = counting_table.rn,
timeslot_group = counting_table.rn
from (
    -- selecting a new row number for only the calendar period of this timeslot.
select row_number() OVER (order by t2.created_at) - 1 as rn, timeslot_date, timeslot_sequence_number, cp.calendar_id as calendar_id from
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



-- Constraints
alter table location_reservations
drop constraint fk_location_reservations_to_timeslot;

alter table timeslots
drop constraint pk_timeslots;

alter table location_reservations
drop constraint pk_location_reservations;

alter table timeslots
drop constraint fk_timeslots_to_calendar_periods;

alter table timeslots
add constraint pk_timeslots PRIMARY KEY (sequence_number);

alter table timeslots
add constraint fk_timeslots_to_locations FOREIGN KEY (location_id) references locations(location_id) on update cascade on delete cascade;


alter table location_reservations
add constraint fk_location_reservations_to_timeslot FOREIGN KEY (timeslot_sequence_number) references timeslots(sequence_number)
        on update cascade
        on delete cascade;

alter table location_reservations
add constraint pk_location_reservations PRIMARY KEY (timeslot_sequence_number, user_augentid);

alter table timeslots
drop column timeslot_sequence_number,
drop column timeslot_date,
drop column calendar_id;


alter table location_reservations
drop column timeslot_seqnr,
drop column calendar_id,
drop column timeslot_date;

drop table calendar_periods;