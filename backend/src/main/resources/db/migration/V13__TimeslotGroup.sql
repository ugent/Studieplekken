UPDATE timeslots
set
    timeslot_group = 0;

CREATE SEQUENCE if not exists timeslot_group_seq;
SELECT setval('timeslot_group_seq',  (SELECT max(timeslot_group)+1 FROM timeslots));
alter table timeslots
alter column timeslot_group set default nextval('timeslot_group_seq');

alter table timeslots
alter column timeslot_group set NOT NULL;