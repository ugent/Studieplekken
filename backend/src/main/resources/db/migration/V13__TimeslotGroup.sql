alter table timeslots
add column repeatable boolean NOT NULL default false;

alter table timeslots
add column timeslot_group_id uuid NOT NULL default '00000000-0000-0000-0000-000000000000';

alter table timeslots
drop column timeslot_group;