alter table timeslots
add column timeslot_date DATE NOT NULL default '2021-07-27';

update timeslots
set
timeslot_date = to_date(isoday_of_week::text || '-' || iso_week::text || '-' || iso_year::text, 'ID-IW-IYYY');

alter table timeslots
drop column isoday_of_week;

alter table timeslots
drop column iso_week;

alter table timeslots
drop column iso_year;