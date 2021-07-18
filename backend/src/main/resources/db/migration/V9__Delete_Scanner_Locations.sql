--
-- drop scanners_location (and corresponding trigger) because all people
-- that are allowed to scan are configured with roles_user_volunteer
--

drop trigger set_timestamp_scanners_location
on public.scanners_location;

drop table public.scanners_location;
