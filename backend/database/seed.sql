
-- Initial seeding of a database with data

/*
 * Setup users
 */
INSERT into users(augentid, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password, institution, admin)
VALUES ('001', 'Bram', 'Van de Walle', 0, 'bram.vandewalle@ugent.be', 'secret', 'UGent', true);

INSERT into users(augentid, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password, institution, admin)
VALUES ('002', 'Ruben_van_DSA', 'DF', 0, 'rdf@ugent.be', 'secret', 'UGent', true);

INSERT into users(augentid, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password, institution, admin)
VALUES ('003', 'Maxime', 'Bloch', 0, 'maxime.bloch@ugent.be', 'secret', 'UGent', true);


/*
 * Setup two test locations with an authority and add 2nd test user to the authority
 */
DO $$
DECLARE tableId integer;
BEGIN
  INSERT INTO authority (authority_name, description)
    VALUES ('DSA', 'Dienst StudentenActiviteiten') RETURNING authority_id into tableId;
  INSERT INTO locations (name, address, number_of_seats, number_of_lockers, image_url, authority_id, description_dutch, description_english)
    VALUES  ('Turbinezaal', 'Hoveniersberg 24, 9000 Gent', 50, 100, '', tableId, 'neder', 'engl'),
            ('Plenaire vergaderzaal', 'Hoveniersberg 24, 9000 Gent', 30, 0, '', tableId, '', ''),
            ('Podiumzaal', 'Hoveniersberg 24, 9000 Gent', 100, 0, '', tableId, '', ''),
            ('Trechterzaal', 'Hoveniersberg 24, 9000 Gent', 80, 0, '', tableId, '', '');
  INSERT INTO roles_user_authority (user_id, authority_id) VALUES ('002',tableId);
END $$;


DO $$
DECLARE new_authority_id integer;
BEGIN
  INSERT INTO authority (authority_name, description) VALUES ('WE', 'Faculteit wetenschappen') RETURNING authority_id into new_authority_id;
  INSERT INTO locations (name, address, number_of_seats, number_of_lockers, image_url, authority_id, description_dutch, description_english)
    VALUES  ('Sterre S9, PC lokaal 3rd verdiep', 'Krijgslaan 281, 9000 Gent', 40, 100, '', new_authority_id, 'Klaslokaal met computers', 'Classroom with computers'),
            ('Sterre S5, Bib', 'Krijgslaan 281, 9000 Gent', 100, 100, '', new_authority_id,
                    'Informatie over de bib kan hier gevonden worden: https://lib.ugent.be/nl/libraries/WEBIB.',
                    'Information about the bib itself can be found here: https://lib.ugent.be/nl/libraries/WEBIB.'),
            ('Sterre S5, Eetzaal', 'Krijgslaan 281, 9000 Gent', 130, 100, '', new_authority_id, '', '');
  INSERT INTO roles_user_authority (user_id, authority_id) VALUES ('002', new_authority_id);
END $$;
/*
 * add tags and use them in location Therminal
 */
DO $$
DECLARE new_tag_id integer;
BEGIN
    INSERT INTO tags (dutch, english) values ('eetplaats', 'dinner place') RETURNING tag_id into new_tag_id;
    INSERT INTO location_tags (location_id, tag_id) values ('Sterre S5, Eetzaal', new_tag_id);

    INSERT INTO tags (dutch, english) values ('stilte', 'silencium') RETURNING tag_id into new_tag_id;
    INSERT INTO location_tags (location_id, tag_id) values ('Sterre S5, Bib', new_tag_id);

    INSERT INTO tags (dutch, english) values ('computers', 'computers') RETURNING tag_id into new_tag_id;
    INSERT INTO location_tags (location_id, tag_id) values ('Sterre S9, PC lokaal 3rd verdiep', new_tag_id);
END $$;
/*
 * Add some calendar periods
 */
insert into calendar_periods(location_name, starts_at, ends_at, opening_time, closing_time, reservable_from)
values  ('Sterre S5, Eetzaal', to_char(now() - interval '5 days', 'YYYY-MM-DD'), to_char(now() + interval '20 days', 'YYYY-MM-DD'),
            '8:30', '21:00', to_char(now() - interval '7 days', 'YYYY-MM-DD') || ' 19:00'),
        ('Sterre S5, Bib', to_char(now() - interval '5 days', 'YYYY-MM-DD'), to_char(now() + interval '10 days', 'YYYY-MM-DD'),
            '09:00', '17:00', to_char(now() - interval '7 days', 'YYYY-MM-DD') || ' 19:00'),
        ('Sterre S9, PC lokaal 3rd verdiep', to_char(now() - interval '5 days', 'YYYY-MM-DD'), to_char(now() + interval '10 days', 'YYYY-MM-DD'),
            '8:30', '18:30', to_char(now() - interval '7 days', 'YYYY-MM-DD') || ' 19:00');

/*
 * Add some calendar periods for lockers
 */
insert into calendar_periods_for_lockers(location_name, starts_at, ends_at, reservable_from)
values ('Sterre S5, Bib', to_char(now() - interval '15 days', 'YYYY-MM-DD'), to_char(now() + interval '5 days', 'YYYY-MM-DD'),
to_char(now() - interval '25 days', 'YYYY-MM-DD') || ' 19:00');

/*
 * Add some penalties for the test user
 */
insert into location_reservations(date, location_name, attended, user_augentid)
values
-- One reservation for over five days
(to_char(now() + interval '5 days', 'YYYY-MM-DD'), 'Sterre S5, Bib', null, '001'),
-- One reservation for five days ago, attended to
(to_char(now() - interval '5 days', 'YYYY-MM-DD'), 'Sterre S5, Bib', true, '001'),
-- One reservation for four days ago, not attended to
(to_char(now() - interval '4 days', 'YYYY-MM-DD'), 'Sterre S5, Eetzaal', false, '001');

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

        insert into lockers (location_name, number)
        values ('Sterre S5, Bib', i);

        insert into lockers (location_name, number)
        values ('Sterre S9, PC lokaal 3rd verdiep', i);

        i := i + 1 ;
    end loop;
END $$;

/*
 * Add some locker reservations for the test user
 */
insert into locker_reservations(location_name, locker_number, user_augentid, key_pickup_date, key_return_date)
-- insert a 'fresh' reservation (keyPickup- and keyReturnedDates are null)
values ('Sterre S9, PC lokaal 3rd verdiep', 0, '001', null, null),
-- insert a reservation where the key has just been picked up
('Sterre S5, Bib', 1, '001', to_char(now(), 'YYYY-MM-DD'), null),
-- insert a reservation where the key has been picked up and has just been returned
('Sterre S5, Bib', 0, '001', to_char(now() - interval '1 month', 'YYYY-MM-DD'),
    to_char(now(), 'YYYY-MM-DD'));
