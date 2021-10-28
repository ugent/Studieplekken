ALTER TABLE location_reservations DROP COLUMN IF EXISTS location_reservation_id;
ALTER TABLE location_reservations DROP COLUMN IF EXISTS state;

--- CREATE SEQUENCE if not exists location_reservation_id_seq;
--- SELECT setval('location_reservation_id_seq',  1);
--- alter table location_reservations
---    add location_reservation_id int UNIQUE NOT NULL default nextval('location_reservation_id_seq');


ALTER TABLE location_reservations DROP CONSTRAINT IF EXISTS pk_location_reservations;
ALTER TABLE location_reservations DROP CONSTRAINT IF EXISTS location_reservations_uniq_timeslot_user;
ALTER TABLE location_reservations ADD CONSTRAINT pk_location_reservations PRIMARY KEY (timeslot_sequence_number, user_id);
ALTER TABLE location_reservations ADD state VARCHAR(20) NOT NULL DEFAULT 'APPROVED';

--- ALTER TABLE location_reservations ADD CONSTRAINT location_reservations_uniq_timeslot_user UNIQUE (timeslot_sequence_number, user_id);
--- ALTER 

-- Remove the old attended state.
UPDATE location_reservations SET state = 'PRESENT' WHERE attended is true;
UPDATE location_reservations SET state = 'ABSENT' WHERE attended is false;
UPDATE location_reservations SET state = 'APPROVED' WHERE attended is null;
ALTER TABLE location_reservations DROP COLUMN IF EXISTS attended;