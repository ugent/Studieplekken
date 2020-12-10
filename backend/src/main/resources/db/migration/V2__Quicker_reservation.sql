Alter table reservation_timeslots
ADD reservation_count INT default 0;

Alter table reservation_timeslots
ADD seat_count INT default 0;

Alter table reservation_timeslots
ADD CONSTRAINT RESERVATION_SEATLIMIT CHECK (reservation_count <= seat_count);

-- fill in seat count
update reservation_timeslots
SET seat_count = ab.number_of_seats
from(
SELECT l2.number_of_seats, cp.calendar_id from locations l2 inner join calendar_periods cp on l2.name = cp.location_name
) ab
where ab.calendar_id = reservation_timeslots.calendar_id;

-- Fill in reserved count
update reservation_timeslots
SET reservation_count = ab.count
FROM (
SELECT count(*), location_reservations.timeslot_date, location_reservations.calendar_id, location_reservations.timeslot_seqnr from location_reservations inner join reservation_timeslots
on
reservation_timeslots.timeslot_date = location_reservations.timeslot_date and
reservation_timeslots.calendar_id = location_reservations.calendar_id and
reservation_timeslots.timeslot_sequence_number = location_reservations.timeslot_seqnr
group by location_reservations.timeslot_date, location_reservations.calendar_id, location_reservations.timeslot_seqnr
) ab
where
reservation_timeslots.timeslot_date = ab.timeslot_date and
reservation_timeslots.calendar_id = ab.calendar_id and
reservation_timeslots.timeslot_sequence_number = ab.timeslot_seqnr;

