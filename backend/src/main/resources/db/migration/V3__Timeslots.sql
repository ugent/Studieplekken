-- Goal of migration: create timeslots for non-reservable calendar periods too, which makes deducing the
-- status of a location a lot easier and more efficient.

-- Step 1: add reservation_timeslots to calendar periods that are not reservable
with recursive x as (
    select * from calendar_periods where reservable = false
), y as (
    select x.calendar_id, 0 as timeslot_sequence_number, x.starts_at as timeslot_date, x.seat_count, x.ends_at
    from x
    union all
    select calendar_id, 0, (timeslot_date + interval '1 day')::date, seat_count, ends_at
    from y
    where timeslot_date + interval '1 day' <= ends_at
)
insert into reservation_timeslots (calendar_id, timeslot_sequence_number, timeslot_date, seat_count)
select calendar_id, timeslot_sequence_number, timeslot_date, seat_count
from y
order by calendar_id, timeslot_date;

-- Step 2: rename RESERVATION_TIMESLOTS to just TIMESLOTS
alter table reservation_timeslots
rename to timeslots;
