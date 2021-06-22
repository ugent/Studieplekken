-- Summary:
--     - drop users_to_verify table
--     - change names of columns containing 'augentid'
--     - split column name forgroup to two words
--

-- drop users_to_verify table: not used
drop table users_to_verify;

-- change names of columns containing 'augentid'
alter table public.users
rename column augentid to user_id;

alter table public.users
rename column augentpreferredgivenname to first_name;

alter table public.users
rename column augentpreferredsn to last_name;

alter table public.location_reservations
rename column user_augentid to user_id;

alter table public.penalty_book
rename column user_augentid to user_id;

alter table public.scanners_location
rename column user_augentid to user_id;

-- split column name forgroup to two words
alter table public.locations
rename column forgroup to for_group;
