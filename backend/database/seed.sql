
-- Initial seeding of a database with data

/*
 * Setup a test user
 */
insert into public.users(augentid, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password, institution, admin)
values ('001', 'Bram', 'Van de Walle', 0, 'bram.vandewalle@ugent.be', 'secret', 'UGent', true);

/*
 * setup second test user
 */
insert into public.users(augentid, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password, institution, admin)
values ('002', 'Ruben_van_DSA', 'DF', 0, 'rdf@ugent.be', 'secret', 'UGent', true);

/*
 * Setup two test locations with an authority and add 2nd test user to the authority
 */
DO $$
DECLARE tableId integer;
BEGIN
  INSERT INTO public.authority (authority_name, description) values ('DSA', 'Dienst StudentenActiviteiten') RETURNING authority_id into tableId;
  INSERT INTO public.locations (name, address, number_of_seats, number_of_lockers, image_url, authority_id, description_dutch, description_english)
    VALUES ('Therminal', 'Hoveniersberg 24, 9000 Gent', 200, 100, '', tableId, 'neder', 'engl'),
            ('Sterre S5', 'Krijgslaan 281, 9000 Gent', 200, 100, '', tableId, 'nederdescr', 'engldescr');
  INSERT INTO public.roles_user_authority (user_id, authority_id) VALUES ('002',tableId);
END $$;
/*
 * add tags and use them in location Therminal
 */
DO $$
DECLARE tableId integer;
BEGIN
    INSERT INTO public.tags (dutch, english) values ('eetplaats', 'dinner place') RETURNING tag_id into tableId;
    INSERT INTO public.location_tags (location_id, tag_id) values ('Therminal', tableId);
    INSERT INTO public.tags (dutch, english) values ('stilte', 'silencium') RETURNING tag_id into tableId;
    INSERT INTO public.location_tags (location_id, tag_id) values ('Therminal', tableId);
END $$;
/*
 * Add some calendar periods
 */
insert into public.calendar_periods(location_name, starts_at, ends_at, opening_time, closing_time, reservable_from, reservable)
values ('Therminal', to_char(now() - interval '5 days', 'YYYY-MM-DD'), to_char(now() + interval '5 days', 'YYYY-MM-DD'),
'09:00', '17:00', to_char(now() - interval '7 days', 'YYYY-MM-DD') || ' 19:00', true),
('Sterre S5', to_char(now() - interval '5 days', 'YYYY-MM-DD'), to_char(now() + interval '5 days', 'YYYY-MM-DD'),
'09:00', '17:00', to_char(now() - interval '7 days', 'YYYY-MM-DD') || ' 19:00', true);

/*
 * Add some calendar periods for lockers
 */
insert into public.calendar_periods_for_lockers(location_name, starts_at, ends_at, reservable_from)
values ('Therminal', to_char(now() - interval '15 days', 'YYYY-MM-DD'), to_char(now() + interval '5 days', 'YYYY-MM-DD'),
to_char(now() - interval '25 days', 'YYYY-MM-DD') || ' 19:00'),
('Sterre S5', to_char(now() - interval '15 days', 'YYYY-MM-DD'), to_char(now() + interval '5 days', 'YYYY-MM-DD'),
to_char(now() - interval '25 days', 'YYYY-MM-DD') || ' 19:00');

/*
 * Add some penalties for the test user
 */
insert into public.location_reservations(date, location_name, attended, user_augentid)
values
-- One reservation for over five days
(to_char(now() + interval '5 days', 'YYYY-MM-DD'), 'Therminal', null, '001'),
-- One reservation for five days ago, attended to
(to_char(now() - interval '5 days', 'YYYY-MM-DD'), 'Therminal', true, '001'),
-- one reservation for four days ago, not attended to
(to_char(now() - interval '4 days', 'YYYY-MM-DD'), 'Sterre S5', false, '001');

/*
 * Setup all the lockers for the test locations
 */
DO $$
DECLARE
   i integer := 0 ;
   n integer := 99;
BEGIN
    loop
        exit when i = n ;

        insert into public.lockers (location_name, number)
        values ('Therminal', i);

        insert into public.lockers (location_name, number)
        values ('Sterre S5', i);

        i := i + 1 ;
    end loop;
END $$;

/*
 * Add some locker reservations for the test user
 */
insert into public.locker_reservations(location_name, locker_number, user_augentid, key_pickup_date, key_return_date)
-- insert a 'fresh' reservation (keyPickup- and keyReturnedDates are null)
values ('Therminal', 0, '001', null, null),
-- insert a reservation where the key has just been picked up
('Therminal', 1, '001', to_char(now(), 'YYYY-MM-DD'), null),
-- insert a reservation where the key has been picked up and has just been returned
('Sterre S5', 0, '001', to_char(now() - interval '1 month', 'YYYY-MM-DD'),
    to_char(now(), 'YYYY-MM-DD'));
