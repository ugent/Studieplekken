drop trigger set_timestamp_locker_reservations
on public.locker_reservations;

drop trigger set_timestamp_lockers
on public.lockers;

drop trigger set_timestamp_calendar_periods_for_lockers
on public.calendar_periods_for_lockers;

drop table public.calendar_periods_for_lockers;
drop table public.locker_reservations;
drop table public.lockers;
