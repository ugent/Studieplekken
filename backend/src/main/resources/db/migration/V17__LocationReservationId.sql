ALTER TABLE location_reservations DROP COLUMN IF EXISTS location_reservation_id;
CREATE SEQUENCE if not exists location_reservation_id_seq;
SELECT setval('location_reservation_id_seq',  1);
alter table location_reservations
    add location_reservation_id int UNIQUE NOT NULL default nextval('location_reservation_id_seq');


ALTER TABLE location_reservations DROP CONSTRAINT IF EXISTS pk_location_reservations;
ALTER TABLE location_reservations ADD CONSTRAINT pk_location_reservations PRIMARY KEY (location_reservation_id);
ALTER TABLE location_reservations DROP CONSTRAINT IF EXISTS location_reservations_uniq_timeslot_user;
ALTER TABLE location_reservations ADD CONSTRAINT location_reservations_uniq_timeslot_user UNIQUE (timeslot_sequence_number, user_id);