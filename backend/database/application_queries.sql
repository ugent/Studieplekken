-- This file contains all the queries that are used within the application.
--
-- However, this file will not be read by Spring Boot to get the SQL queries.
-- The Java code uses the file database.properties as a key-value file to
-- get the queries.
--
-- This file is used as it is much easier to read/write/update formatted
-- queries. But, we need to convert this file into the key-value format.
-- Converting these queries can be done by:
--    - Windows: run `backend/scripts/update_database_properties.bat
--    - Unix: run `backend/scripts/update_database_properties.sh` with bash
--
-- Not only the queries in this file will be added to the database.properties
-- file, but the key-values within application_columns.props will be added
-- to the file as well.
--
-- Do note that it is important that this file is formatted correctly in
-- order for the formatting script to be able to correctly create the
-- database.properties file:
--     +-
--     | -- Start of a new block of queries, e.g. queries for a TABLE
--     | -- $name_of_query1.1
--     | ...
--     |  formatted query that must end with ';'
--     | ...;
--     |
--     | -- $name_of_query1.2 (after one newline)
--     | ...
--     |  formatted query that must end with ';'
--     | ...;
--     |
--     |
--     | -- Start of a new block of queries, after two \n
--     | -- $name_of_query2.1
--     | ...
--     |  formatted query that must end with ';'
--     | ...;
--     +-

-- queries for table LOCATIONS
-- $all_locations
select l.name, l.number_of_seats, l.number_of_lockers
    , l.image_url, l.address, l.authority_id
from public.locations l
order by l.name;

-- $get_location
select l.name, l.number_of_seats, l.number_of_lockers
    , l.image_url, l.address, l.authority_id
from public.locations l
where l.name = ?;

-- $get_locations_from_authority
select  l.name, l.number_of_seats, l.number_of_lockers
     , l.image_url, l.address, l.authority_id
from public.locations l
where authority_id = ?;

-- $delete_location
delete
from public.locations
where name = ?;

-- $delete_locations_from_authority
delete
from public.locations
where authority_id = ?;

-- $insert_location
insert into public.locations (name, number_of_seats, number_of_lockers, image_url, address, authority_id)
values (?, ?, ?, ?, ?, ?);

-- $update_location
update public.locations
set name = ?, number_of_seats = ?, number_of_lockers = ?, image_url = ?, address = ?, authority_id = ?
where name = ?;


-- queries for table LOCATION_RESERVATION
-- $get_location_reservations_where_<?>
/*
    If you want to change the weekly percentage decrease, you must
    change the factor and amount of weeks used in the recursive query.
    Now, every week the amount is reduced with 20%, which means that points
    will remain effective for 5 weeks.

    If you would like to change this to 10% (and an effectiveness of 10 weeks),
    then you'll have to change "- 0.2 * (week + 1) + 1" to "- 0.1 * (week + 1) + 1"
    and "week + 1 <= 5" to "week + 1 <= 10".

    Note: change get_user_by_<?> as well for consistency
*/
with recursive x as (
    select 0 week, 1.0 perc
        union all
    select week + 1, - 0.2 * (week + 1) + 1
    from x
    where week + 1 <= 5
), y as (
	select u.mail, u.augentpreferredsn, u.augentpreferredgivenname, u.password, u.institution
		, u.augentid, u.role
		, lr.date, lr.location_name, lr.attended, lr.user_augentid
		, coalesce(floor(sum(
        	case
				/*
					Blacklist event (16662) is permanent: no decrease in time.
					A blacklist event should be removed manually
				*/
				when pb.event_code = 16662 then pb.received_points
				else pb.received_points * x.perc
			end)), 0) as "penalty_points"
	from public.location_reservations lr
		join public.users u
			on u.augentid = lr.user_augentid
		left join public.penalty_book pb
			on pb.user_augentid = u.augentid
		left join x
			on floor(extract(days from (now() - to_timestamp(pb.timestamp, 'YYYY-MM-DD HH24\:MI\:SS'))) / 7) = x.week
	where <?>
	group by u.mail, u.augentpreferredsn, u.augentpreferredgivenname, u.password, u.institution
		, u.augentid, u.role
		, lr.date, lr.location_name, lr.attended, lr.user_augentid
)
select y.mail, y.augentpreferredsn, y.augentpreferredgivenname, y.password, y.institution
	 , y.augentid, y.role, y.penalty_points
	 , y.date, y.location_name, y.attended, y.user_augentid
	 , l.name, l.number_of_seats, l.number_of_lockers, l.image_url, l.address, l.authority_id
from y
    join public.locations l
        on l.name = y.location_name
group by y.mail, y.augentpreferredsn, y.augentpreferredgivenname, y.password, y.institution
	 , y.augentid, y.role, y.penalty_points
	 , y.date, y.location_name, y.attended, y.user_augentid
	 , l.name, l.number_of_seats, l.number_of_lockers, l.image_url, l.address, l.authority_id
order by l.name;

-- $count_location_reservations_of_location_for_date
select count(1)
from public.location_reservations
where location_name = ? and date = ?;

-- $delete_location_reservation
delete
from public.location_reservations
where user_augentid = ? and date = ?;

-- $delete_location_reservations_of_location
delete
from public.location_reservations
where location_name = ?;

-- $delete_location_reservations_of_user
delete from public.location_reservations
where user_augentid = ?;

-- $delete_location_reservations_of_location_between_dates
delete
from public.location_reservations
where location_name = ? and cast(substr(date,0,5) as int)*404 + cast(substr(date,6,2) as int)*31 + cast(substr(date,9,2) as int) between ? and ?;

-- $insert_location_reservation
insert into public.location_reservations (date, location_name, user_augentid, attended)
values (?, ?, ?, null);

-- $set_location_reservation_unattended
update public.location_reservations
set attended = false
where date = ? and user_augentid = ?;

-- $set_all_location_reservations_attended
update public.location_reservations
set attended = true
where location_name = ? and date = ?;

-- $set_location_reservation_attended
update public.location_reservations
set attended = true
where date = ? and user_augentid = ?;

-- $count_location_reservations_on_date
select l.name, count(case when lr.date = ? then 1 end)
from public.locations l
    left join public.location_reservations lr
        on l.name = lr.location_name
group by l.name
order by l.name;

-- $update_fk_location_reservations_to_location
update public.location_reservations
set location_name = ?
where location_name = ?;

-- $update_fk_location_reservations_to_user
update public.location_reservations
set user_augentid = ?
where user_augentid = ?;


-- queries for table USER
-- $get_user_by_<?>
/*
    If you want to change the weekly percentage decrease, you must
    change the factor and amount of weeks used in the recursive query.
    Now, every week the amount is reduced with 20%, which means that points
    will remain effective for 5 weeks.

    If you would like to change this to 10% (and an effectiveness of 10 weeks),
    then you'll have to change "- 0.2 * (week + 1) + 1" to "- 0.1 * (week + 1) + 1"
    and "week + 1 <= 5" to "week + 1 <= 10".

    Note: change get_location_reservations_where_<?> as well for consistency
*/
with recursive x as (
    select 0 week, 1.0 perc
        union all
    select week + 1, - 0.2 * (week + 1) + 1
    from x
    where week + 1 <= 5
)
select u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn
    , u.mail, u.password, u.institution
    , coalesce(floor(sum(
        case
            /*
                Blacklist event is permanent: no decrease in time.
                A blacklist event should be removed manually
            */
            when b.event_code = 16662 then b.received_points
            else b.received_points * x.perc
        end)), 0) as "penalty_points"
from public.users u
    left join public.penalty_book b
        on b.user_augentid = u.augentid
    left join x
        on floor(extract(days from (now() - to_timestamp(b.timestamp, 'YYYY-MM-DD HH24\:MI\:SS'))) / 7) = x.week
where <?>
group by u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.mail, u.password, u.institution
order by u.augentpreferredsn, u.augentpreferredgivenname, u.augentid;

-- $update_user
update public.users
set mail = ?, augentpreferredsn = ?, augentpreferredgivenname = ?, password = ?, institution = ?, augentid = ?, role = ?, penalty_points = ?
where augentid = ?;

-- $count_accounts_with_email
select count(1)
from public.users
where LOWER(mail) = LOWER(?);

-- $insert_user
insert into public.users (mail, augentpreferredsn, augentpreferredgivenname, password, institution, augentid, role, penalty_points)
values (?, ?, ?, ?, ?, ?, ?, ?);

-- $delete_user
delete
from public.users
where augentid = ?;

-- $set_mail_of_user_by_id
update public.users
set mail = ?
where augentid = ?;


-- queries for table USERS_TO_VERIFY
-- $count_user_to_be_verified_by_id
select count(1)
from public.users_to_verify
where augentid = ?;

-- $insert_user_to_be_verified
insert into public.users_to_verify (mail, augentpreferredsn, augentpreferredgivenname, password, institution, augentid, role, verification_code, created_timestamp)
values (?, ?, ?, ?, ?, ?, ?, ?, ?);

-- $get_user_to_be_verfied_by_verification_code
select *
from public.users_to_verify
where verification_code = ?;

-- $delete_user_to_be_verfied
delete
from public.users_to_verify
where verification_code = ?;

-- $daily_cleanup_user_to_be_verified
delete
from public.users_to_verify
where TO_TIMESTAMP(created_timestamp, 'YYYY-MM-DD\\THH24:MI:SS') < now() - interval '1 days';


-- queries for table ROLES_USER_AUTHORITY
-- $delete_roles_user_authority_of_user
delete from public.roles_user_authority
where user_id = ?;

-- $delete_roles_user_authority_of_authority
delete
from public.roles_user_authority
where authority_id = ?;

-- $update_fk_roles_user_authority_to_user
update public.roles_user_authority
set user_id = ?
where user_id = ?;

-- $insert_role_user_authority
insert into public.roles_user_authority (user_id, authority_id)
values (?, ?);

-- $remove_role_user_authority
delete
from public.roles_user_authority
where user_id = ? and authority_id = ?;


-- queries for table AUTHORITY
-- $all_authorities
select a.authority_id, a.name, a.description
from public.authority a
order by a.name;

-- $authorities_from_user
select a.authority_id, a.name, a.description
from public.authority a
  join public.roles_user_authority roles on a.authority_id = roles.authority_id
  join public.users u on roles.user_id = u.augentid
  where u.augentid = ?
order by a.name;

-- $authority_get_users
select u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.penalty_points, u.mail, u.institution
from public.users u
         join public.roles_user_authority roles on u.augentid = roles.user_id
         join public.authority a on roles.authority_id = a.authority_id
where a.authority_id = ?
order by u.augentid;

-- $authority_from_name
select a.authority_id, a.name, a.description
from public.authority a
where a.name = ?;

-- $authority_from_authority_id
select a.authority_id, a.name, a.description
from public.authority a
where a.authority_id = ?;

-- $insert_authority
insert into public.authority (name, description)
values (?, ?) RETURNING authority_id;

-- $update_authority
update public.authority
set name = ?, description = ?
where authority_id = ?;

-- $delete_authority
delete
from public.authority
where authority_id = ?;


-- queries for table LOCKER_RESERVATIONS
-- $get_locker_reservations_where_<?>
with recursive x as (
    select 0 week, 1.0 perc
        union all
    select week + 1, - 0.2 * (week + 1) + 1
    from x
    where week + 1 <= 5
), y as (
	select u.mail, u.augentpreferredsn, u.augentpreferredgivenname, u.password, u.institution
		, u.augentid, u.role
		, l.location_name, l.number
		, lr.user_augentid, lr.key_pickup_date, lr.key_return_date
		, coalesce(floor(sum(
        	case
				/*
					Blacklist event (16662) is permanent: no decrease in time.
					A blacklist event should be removed manually
				*/
				when pb.event_code = 16662 then pb.received_points
				else pb.received_points * x.perc
			end)), 0) as "penalty_points"
	from public.locker_reservations lr
		join public.lockers l
			on l.location_name = lr.location_name
	        and l.number = lr.locker_number
		join public.users u
			on u.augentid = lr.user_augentid
		left join public.penalty_book pb
			on pb.user_augentid = u.augentid
		left join x
			on floor(extract(days from (now() - to_timestamp(pb.timestamp, 'YYYY-MM-DD HH24\:MI\:SS'))) / 7) = x.week
	where <?>
	group by u.mail, u.augentpreferredsn, u.augentpreferredgivenname, u.password, u.institution
		, u.augentid, u.role
		, l.location_name, l.number
		, lr.user_augentid, lr.key_pickup_date, lr.key_return_date
)
select y.mail, y.augentpreferredsn, y.augentpreferredgivenname, y.password, y.institution
     , y.augentid, y.role, y.penalty_points
     , y.number, y.location_name
     , y.user_augentid, y.key_pickup_date, y.key_return_date
	 , l.name, l.number_of_seats, l.number_of_lockers, l.image_url, l.address, l.authority_id
from y
	join public.locations l
		on l.name = y.location_name
group by y.mail, y.augentpreferredsn, y.augentpreferredgivenname, y.password, y.institution
     , y.augentid, y.role, y.penalty_points
     , y.number, y.location_name
     , y.user_augentid, y.key_pickup_date, y.key_return_date
	 , l.name, l.number_of_seats, l.number_of_lockers, l.image_url, l.address, l.authority_id
order by l.name;

-- $count_lockers_in_use_of_location
select count(1)
from public.locker_reservations r
    join public.lockers l
        on r.location_name = l.location_name
        and r.locker_number = l.number
where l.location_name = ? and r.key_pickup_date <> '' and r.key_return_date = '';

-- $delete_locker_reservation
delete
from public.locker_reservations
where location_name = ? and locker_number = ?;

-- $delete_locker_reservations_of_user
delete
from public.locker_reservations
where user_augentid = ?;

-- $delete_locker_reservations_in_location
delete
from public.locker_reservations
where location_name = ?;

-- $insert_locker_reservation
insert into public.locker_reservations (location_name, locker_number, user_augentid, key_pickup_date, key_return_date)
values (?, ?, ?, ?, ?);

-- $update_locker_reservation
update public.locker_reservations
set key_pickup_date = ?, key_return_date = ?
where location_name = ? and locker_number = ?;

-- $update_fk_locker_reservations_to_location
update public.locker_reservations
set location_name = ?
where location_name = ?;

-- $update_fk_locker_reservations_to_user
update public.locker_reservations
set user_augentid = ?
where user_augentid = ?;


-- queries for table LOCKERS
-- $get_lockers_where_<?>
select l.location_name, l.number
	, s.name, s.number_of_seats, s.number_of_lockers, s.image_url, s.address, s.authority_id
from public.lockers l
	join public.locations s
		on s.name = l.location_name
where <?>
order by s.name;

-- $get_lockers_statuses_of_location
with recursive x as (
    select l.number_of_lockers, l.name
    from public.locations l
    where l.name = ?
), y as (
    select -1 as locker_number, '' location_name
        union all
    select locker_number + 1, x.name
    from y, x
    where y.locker_number + 1 < x.number_of_lockers
), z as (
    select location_name, locker_number
    from y
    where locker_number >= 0
), lr as (
    select location_name, locker_number, user_augentid, key_pickup_date, key_return_date
    from public.locker_reservations
    where key_return_date = '' or key_return_date is NULL
)
select z.location_name, z.locker_number as number,
       l.name, l.number_of_seats, l.number_of_lockers, l.image_url, l.address, l.authority_id,
       lr.location_name, lr.locker_number, lr.user_augentid, lr.key_pickup_date, lr.key_return_date,
       u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn,
       u.penalty_points, u.mail, u.password, u.institution
from z
    join public.locations l
        on l.name = z.location_name
    left join lr on
        lr.location_name = z.location_name and
        lr.locker_number = z.locker_number
    left join public.users u
        on u.augentid = lr.user_augentid
order by z.locker_number;

-- $delete_lockers_of_location
delete
from public.lockers
where location_name = ?;

-- $delete_lockers_of_location_from_number
delete
from public.lockers
where location_name = ? and number >= ?;

-- $insert_locker
/* Note: the column 'id' is a auto-increment primary key */
insert into public.lockers (number, location_name)
values (?, ?);

-- $delete_locker
delete
from public.lockers
where location_name = ? and number = ?;


-- queries for table ROLES
-- $get_roles
select *
from public.roles;


-- queries for DBPenaltyEventsDao
-- $get_penalty_events
select e.code, e.points, d.lang_enum, d.description
from penalty_events e
    join penalty_descriptions d
        on e.code = d.event_code
order by e.code;

-- $get_penalty_event
select e.code, e.points, d.lang_enum, d.description
from penalty_events e
    join penalty_descriptions d
        on e.code = d.event_code
where e.code = ?;

-- $get_penalties_by_user
select b.user_augentid, b.event_code, b.timestamp, b.reservation_date
    , b.received_points, b.reservation_location, b.remarks
from public.penalty_book b
where b.user_augentid = ?;

-- $get_penalties_by_location
select b.user_augentid, b.event_code, b.timestamp, b.reservation_date
     , b.received_points, b.reservation_location, b.remarks
from public.penalty_book b
where b.reservation_location = ?;

-- $get_penalties_by_event_code
select b.user_augentid, b.event_code, b.timestamp, b.reservation_date
     , b.received_points, b.reservation_location, b.remarks
from public.penalty_book b
where b.event_code = ?;

-- $insert_penalty_event
insert into public.penalty_events (code, points)
values (?, ?);

-- $insert_penalty
insert into penalty_book (user_augentid, event_code, timestamp, reservation_date, reservation_location, received_points, remarks)
values (?, ?, ?, ?, ?, ?, ?);

-- $insert_penalty_description
insert into public.penalty_descriptions (lang_enum, event_code, description)
values (?, ?, ?);

-- $update_penalty_event
update public.penalty_events
set points = ?
where code = ?;

-- $update_fk_penalty_book_to_locations
update public.penalty_book
set reservation_location = ?
where reservation_location = ?;

-- $update_fk_penalty_book_to_penalty_event
update public.penalty_book
set event_code = ?
where event_code = ?;

-- $update_fk_penalty_book_to_user
update public.penalty_book
set user_augentid = ?
where user_augentid = ?;

-- $delete_penalty_description
delete
from public.penalty_descriptions
where lang_enum = ? and event_code = ?;

-- $delete_penalty_descriptions_by_event_code
delete
from public.penalty_descriptions
where event_code = ?;

-- $delete_penalty_event
delete
from public.penalty_events
where code = ?;

-- $delete_penalty
delete
from public.penalty_book b
where b.user_augentid = ? and b.event_code = ? and b.timestamp = ?;

-- $delete_penalties_of_location
delete
from public.penalty_book
where reservation_location = ?;

-- $delete_penalties_of_penalty_event
delete
from public.penalty_book
where event_code = ?;

-- $delete_penalties_of_user
delete
from public.penalty_book
where user_augentid = ?;

-- $count_descriptions_of_penalty_events
select count(1)
from public.penalty_descriptions
where event_code = ?;


-- queries for SCANNERS_LOCATION
-- $get_locations_of_scanner
select l.name, l.number_of_seats, l.number_of_lockers, l.image_url, l.address, l.authority_id
from public.scanners_location sl
    join public.locations l
        on l.name = sl.location_name
where sl.user_augentid = ?
order by l.name;

-- $get_scanners_of_location
with recursive x as (
    select 0 week, 1.0 perc
    union all
    select week + 1, - 0.2 * (week + 1) + 1
    from x
    where week + 1 <= 5
)
select u.augentid, u.role, u.augentpreferredsn, u.augentpreferredgivenname
    , u.mail, u.password, u.institution
    , coalesce(floor(sum(
        case
            /*
                Blacklist event (16662) is permanent: no decrease in time.
                A blacklist event should be removed manually
            */
            when pb.event_code = 16662 then pb.received_points
            else pb.received_points * x.perc
            end)), 0) as "penalty_points"
from public.scanners_location sl
    join public.users u
        on u.augentid = sl.user_augentid
    left join public.penalty_book pb
              on pb.user_augentid = u.augentid
    left join x
              on floor(extract(days from (now() - to_timestamp(pb.timestamp, 'YYYY-MM-DD HH24\:MI\:SS'))) / 7) = x.week
where sl.location_name = ?
group by u.augentid, u.role, u.augentpreferredsn, u.augentpreferredgivenname
    , u.mail, u.password, u.institution;

-- $delete_scanner_location
delete
from public.scanners_location
where location_name = ? and user_augentid = ?;

-- $delete_scanners_of_location
delete from public.scanners_location
where location_name = ?;

-- $delete_locations_of_scanner
delete from public.scanners_location
where user_augentid = ?;

-- $insert_scanner_on_location
insert into public.scanners_location (location_name, user_augentid)
values (?, ?);

-- $count_scanner_on_location
select count(1)
from public.scanners_location
where user_augentid = ? and location_name = ?;

-- $update_fk_scanners_location_to_locations
update public.scanners_location
set location_name = ?
where location_name = ?;

-- $update_fk_scanners_location_to_user
update public.scanners_location
set user_augentid = ?
where user_augentid = ?;


-- queries for CALENDAR_PERIODS
-- $get_calendar_periods
select cp.location_name, cp.starts_at, cp.ends_at, cp.opening_time, cp.closing_time, cp.reservable_from
       , l.name, l.number_of_seats, l.number_of_lockers, l.image_url, l.address, l.authority_id
from public.calendar_periods cp
    join public.locations l
        on l.name = cp.location_name
where cp.location_name = ?
order by to_date(cp.starts_at, 'YYYY-MM-DD');

-- $insert_calendar_period
insert into public.calendar_periods(location_name, starts_at, ends_at, opening_time, closing_time, reservable_from)
values (?, ?, ?, ?, ?, ?);

-- $update_calendar_period
update public.calendar_periods
set location_name = ?, starts_at = ?, ends_at = ?, opening_time = ?, closing_time = ?, reservable_from = ?
where location_name = ? and starts_at = ? and ends_at = ? and opening_time = ? and closing_time = ? and reservable_from = ?;

-- $delete_calendar_period
delete
from public.calendar_periods
where location_name = ? and starts_at = ? and ends_at = ? and opening_time = ? and closing_time = ? and reservable_from = ?;

-- $delete_calendar_periods_of_location
delete
from public.calendar_periods
where location_name = ?;

-- $update_fk_location_name_in_calendar_periods
update public.calendar_periods
set location_name = ?
where location_name = ?;


-- queries for CALENDAR_PERIODS_FOR_LOCKERS
-- $get_calendar_periods_for_lockers_of_location
select cp.location_name, cp.starts_at, cp.ends_at, cp.reservable_from
       , l.name, l.number_of_seats, l.number_of_lockers, l.image_url, l.address, l.authority_id
from public.calendar_periods_for_lockers cp
    join public.locations l
        on l.name = cp.location_name
where cp.location_name = ?
order by to_date(cp.starts_at, 'YYYY-MM-DD');

-- $insert_calendar_period_for_lockers
insert into public.calendar_periods_for_lockers (location_name, starts_at, ends_at, reservable_from)
values (?, ?, ?, ?);

-- $update_calendar_period_for_lockers
update public.calendar_periods_for_lockers
set location_name = ?, starts_at = ?, ends_at = ?, reservable_from = ?
where location_name = ? and starts_at = ? and ends_at = ? and reservable_from = ?;

-- $delete_calendar_period_for_lockers
delete
from public.calendar_periods_for_lockers
where location_name = ? and starts_at = ? and ends_at = ? and reservable_from = ?;

-- $delete_calendar_periods_for_lockers_of_location
delete
from public.calendar_periods_for_lockers
where location_name = ?;

-- $update_fk_location_name_in_calendar_periods_for_lockers
update public.calendar_periods_for_lockers
set location_name = ?
where location_name = ?;