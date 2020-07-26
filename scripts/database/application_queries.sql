-- queries for table LOCATIONS
-- $all_locations
select l.name, l.number_of_seats, l.number_of_lockers
    , l.maps_frame, l.image_url, l.address, l.start_period_lockers
    , l.end_period_lockers, ld.lang_enum, ld.description
from public.locations l
    join public.location_descriptions ld
        on l.name = ld.location_name;

-- $get_location
select l.name, l.number_of_seats, l.number_of_lockers
    , l.maps_frame, l.image_url, l.address, l.start_period_lockers
    , l.end_period_lockers, ld.lang_enum, ld.description
from public.locations l
    join public.location_descriptions ld
        on l.name = ld.location_name
where l.name = ?;

-- $delete_location
delete
from public.locations
where name = ?;

-- $insert_location
insert into public.locations (name, number_of_seats, number_of_lockers, maps_frame, image_url, address, start_period_lockers, end_period_lockers)
values (?, ?, ?, ?, ?, ?, ?, ?);

-- $update_location
update public.locations
set name = ?, number_of_seats = ?, number_of_lockers = ?, maps_frame = ?, image_url = ?, address = ?, start_period_lockers = ?, end_period_lockers = ?
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
	 , l.name, l.number_of_seats, l.number_of_lockers, l.maps_frame, l.image_url, l.address
     , l.start_period_lockers, l.end_period_lockers
     , ld.lang_enum, ld.description
from y
    join public.locations l
        on l.name = y.location_name
    join public.location_descriptions ld
        on ld.location_name = l.name
group by y.mail, y.augentpreferredsn, y.augentpreferredgivenname, y.password, y.institution
	 , y.augentid, y.role, y.penalty_points
	 , y.date, y.location_name, y.attended, y.user_augentid
	 , l.name, l.number_of_seats, l.number_of_lockers, l.maps_frame, l.image_url, l.address
     , l.start_period_lockers, l.end_period_lockers
     , ld.lang_enum, ld.description
order by l.name;

-- $count_location_reservations_of_location_for_date
select count(1)
from public.location_reservations
where location_name = ? and date = ?;

-- $set_all_location_reservations_attended
update public.location_reservations
set attended = true
where location_name = ? and date = ?;

-- $delete_location_reservation
delete
from public.location_reservations
where user_augentid = ? and date = ?;

-- $delete_location_reservations_of_location
delete
from public.location_reservations
where location_name = ?;

-- $delete_location_reservations_of_location_between_dates
delete
from public.location_reservations
where location_name = ? and cast(substr(date,0,5) as int)*404 + cast(substr(date,6,2) as int)*31 + cast(substr(date,9,2) as int) between ? and ?;

-- $delete_location_reservations_of_user_by_id
delete from public.location_reservations
where user_augentid = ?;

-- $insert_location_reservation
insert into public.location_reservations (date, location_name, user_augentid, attended)
values (?, ?, ?, null);

-- $set_location_reservation_unattended
update public.location_reservations
set attended = false
where date = ? and user_augentid = ?;

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

-- $update_location_reservations_of_user
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
group by u.augentid, u.role, u.augentpreferredgivenname, u.augentpreferredsn, u.mail, u.password, u.institution;

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



-- queries for table LOCKER_RESERVATION
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
	 , l.name, l.number_of_seats, l.number_of_lockers, l.maps_frame, l.image_url, l.address
     , l.start_period_lockers, l.end_period_lockers
	 , ld.lang_enum, ld.description
from y
	join public.locations l
		on l.name = y.location_name
	join public.location_descriptions ld
		on ld.location_name = l.name
group by y.mail, y.augentpreferredsn, y.augentpreferredgivenname, y.password, y.institution
     , y.augentid, y.role, y.penalty_points
     , y.number, y.location_name
     , y.user_augentid, y.key_pickup_date, y.key_return_date
	 , l.name, l.number_of_seats, l.number_of_lockers, l.maps_frame, l.image_url, l.address
     , l.start_period_lockers, l.end_period_lockers
	 , ld.lang_enum, ld.description
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

-- $delete_locker_reservations_of_user_by_id
delete
from public.locker_reservations
where user_augentid = ?;

-- $insert_locker_reservation
insert into public.locker_reservations (location_name, locker_number, user_augentid, key_pickup_date, key_return_date)
values (?, ?, ?, ?, ?);

-- $update_locker_reservation
update public.locker_reservations
set key_pickup_date = ?, key_return_date = ?
where location_name = ? and locker_number = ?;

-- $update_locker_reservations_of_user
/*
  This might seem a strange query but is used
  when a user's augentID might have been changed
*/
update public.locker_reservations
set user_augentid = ?
where user_augentid = ?;



-- queries for table LOCKER
-- $get_lockers_where_<?>
select l.location_name, l.number
	, s.name, s.number_of_seats, s.number_of_lockers, s.maps_frame, s.image_url
	, s.address, s.start_period_lockers, s.end_period_lockers
	, sd.lang_enum, sd.description
from public.lockers l
	join public.locations s
		on s.name = l.location_name
	join public.location_descriptions sd
		on sd.location_name = s.name
where <?>
order by s.name;

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

-- $change_locker_location
update public.lockers
set location_name = ?
where location_name = ?;



-- queries for table CALENDAR
-- $get_calendar_of_location
select *
from public.calendar
where location_name = ?;

-- $get_calendar_day_count
select count(1)
from public.calendar
where date = ? and location_name = ?;

-- $insert_calendar_day
insert into public.calendar (date, location_name, opening_time, closing_time, open_for_reservation_date)
values (?, ?, ?, ?, ?);

-- $delete_calendar_of_location
delete
from public.calendar
where location_name = ?;

-- $delete_calendar_day_of_location
delete
from public.calendar
where location_name = ? and date = ?;

-- $delete_calendar_days_between_dates
delete
from public.calendar
where location_name = ? and cast(substr(date,0,5) as int)*404 + cast(substr(date,6,2) as int)*31 + cast(substr(date,9,2) as int) between ? and ?;

-- $update_calendar_day_of_location
update public.calendar
set opening_time = ?, closing_time = ?, open_for_reservation_date = ?
where location_name = ? and date = ?;

-- $update_location_calendar
update public.calendar
set location_name = ?
where location_name = ?;



-- queries for table ROLES
-- $get_roles
select *
from public.roles;



-- queries for DBPenaltyEventsDao
-- $get_penalty_events
select e.code, e.points, e.public_accessible, d.lang_enum
    , d.description
from penalty_events e
    join penalty_descriptions d
        on e.code = d.event_code;

-- $get_penalty_event
select e.code, e.points, e.public_accessible, d.lang_enum
    , d.description
from penalty_events e
    join penalty_descriptions d
        on e.code = d.event_code
where e.code = ?;

-- $get_penalties
select b.user_augentid, b.event_code, b.timestamp, b.reservation_date
    , b.received_points, b.reservation_location
from public.penalty_book b
where b.user_augentid = ?;

-- $insert_penalty_event
insert into public.penalty_events (code, points, public_accessible)
values (?, ?, ?);

-- $insert_penalty
insert into penalty_book (user_augentid, event_code, timestamp, reservation_date, reservation_location, received_points)
values (?, ?, ?, ?, ?, ?);

-- $count_penalty_events_with_code
select count(1)
from penalty_events
where code = ?;

-- $insert_penalty_description
insert into public.penalty_descriptions (lang_enum, event_code, description)
values (?, ?, ?);

-- $update_penalty_event
update public.penalty_events
set points = ?, public_accessible = ?
where code = ?;

-- $update_penalty_description
update public.penalty_descriptions
set description = ?
where lang_enum = ? and event_code = ?;

-- $update_penalties_of_user
update public.penalty_book
set user_augentid = ?
where user_augentid = ?;

-- $delete_penalty_description
delete
from public.penalty_descriptions
where lang_enum = ? and event_code = ?;

-- $delete_penalty_event
delete
from public.penalty_events
where code = ?;

-- $delete_penalty
delete
from public.penalty_book b
where b.user_augentid = ? and b.event_code = ? and b.timestamp = ?;

-- $delete_penalties_of_user_by_id
delete
from public.penalty_book
where user_augentid = ?;

-- $delete_penalties_of_location
delete
from public.penalty_book
where reservation_location = ?;



-- queries for 'scanners_location'
-- $get_locations_of_scanner
select location_name
from public.scanners_location
where user_augentid = ?;

-- $get_scanners_of_location
select user_augentid
from public.scanners_location
where location_name = ?;

-- $delete_scanners_of_location
delete from public.scanners_location
where location_name = ?;

-- $delete_scanners_of_location_of_user_by_id
delete from public.scanners_location
where user_augentid = ?;

-- $insert_scanner_and_location
insert into public.scanners_location (user_augentid, location_name)
values (?, ?);

-- $update_scanners_of_location_of_user
update public.scanners_location
set user_augentid = ?
where user_augentid = ?;



-- queries for 'location_descriptions'
-- $delete_location_descriptions
delete
from public.location_descriptions
where location_name = ?;

-- $insert_location_descriptions
insert into public.location_descriptions (location_name, lang_enum, description)
values (?, ?, ?);
