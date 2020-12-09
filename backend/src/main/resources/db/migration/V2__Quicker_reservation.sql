Alter table reservation_timeslots
ADD reservation_count INT;

Alter table reservation_timeslots
ADD seat_count INT;

Alter table reservation_timeslots
ADD CHECK (reservation_count >= seat_count);