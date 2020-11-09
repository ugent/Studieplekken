
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
 * Setup third test user
 */
insert into public.users(augentid, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password, institution, admin)
values ('01405190', 'Tim', 'Van Erum', 0, 'tim.vanerum@ugent.be', 'secret', 'UGent', true);

/*
 * Setup two test locations with an authority and add 2nd test user to the authority
 */
DO $$
DECLARE tableId integer;
BEGIN
  INSERT INTO public.authority (authority_name, description) values ('DSA', 'Dienst StudentenActiviteiten') RETURNING authority_id into tableId;
  INSERT INTO public.locations (name, address, number_of_seats, number_of_lockers, image_url, authority_id, description_dutch, description_english, forGroup)
    VALUES  ('Turbinezaal', 'Hoveniersberg 24, 9000 Gent', 50, 100, '', tableId, 'neder', 'engl', false),
                ('Plenaire vergaderzaal', 'Hoveniersberg 24, 9000 Gent', 30, 0, '', tableId, '', '', true),
                ('Podiumzaal', 'Hoveniersberg 24, 9000 Gent', 100, 0, '', tableId, '', '', false),
                ('Trechterzaal', 'Hoveniersberg 24, 9000 Gent', 80, 0, '', tableId, '', '', false);
  INSERT INTO public.roles_user_authority (user_id, authority_id) VALUES ('002',tableId);
END $$;


DO $$
DECLARE new_authority_id integer;
BEGIN
  INSERT INTO authority (authority_name, description) VALUES ('WE', 'Faculteit wetenschappen') RETURNING authority_id into new_authority_id;
  INSERT INTO locations (name, address, number_of_seats, number_of_lockers, image_url, authority_id, description_dutch, description_english, forGroup)
    VALUES  ('Sterre S9, PC lokaal 3rd verdiep', 'Krijgslaan 281, 9000 Gent', 40, 100, '', new_authority_id, 'Klaslokaal met computers', 'Classroom with computers', false),
            ('Sterre S5, Bib', 'Krijgslaan 281, 9000 Gent', 100, 100, '', new_authority_id,
                    'Informatie over de bib kan hier gevonden worden: https://lib.ugent.be/nl/libraries/WEBIB.',
                    'Information about the bib itself can be found here: https://lib.ugent.be/nl/libraries/WEBIB.', false),
            ('Sterre S5, Eetzaal', 'Krijgslaan 281, 9000 Gent', 130, 100, '', new_authority_id, '', '', false);
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
insert into calendar_periods(location_name, starts_at, ends_at, opening_time, closing_time, reservable_from, reservable, timeslot_length)
values  ('Sterre S5, Eetzaal', now() - interval '1 days', now() + interval '3 days',
            '10:00', '12:00', now() - interval '7 days', true, 60),
        ('Sterre S5, Bib', now() - interval '5 days', now() + interval '10 days',
            '09:00', '17:00', now() - interval '7 days', false, 0),
        ('Sterre S9, PC lokaal 3rd verdiep',now() - interval '5 days', now() + interval '10 days',
            '8:30', '18:30', now() - interval '7 days', false, 0);


insert into reservation_timeslots(calendar_id, timeslot_sequence_number, timeslot_date)
values 
(1, 0,  now() - interval '1 days'),
(1, 1,  now() - interval '1 days'),
(1, 0,  now()),
(1, 1,  now()),
(1, 0,  now()+ interval '1 days'),
(1, 1,  now()+ interval '1 days'),
(1, 0,  now()+ interval '2 days'),
(1, 1,  now()+ interval '2 days'),
(1, 0,  now()+ interval '3 days'),
(1, 1,  now()+ interval '3 days');


/*
 * Add some penalties for the test user
 */
insert into location_reservations(created_at, timeslot_date, timeslot_seqnr, calendar_id, user_augentid)
values
-- One reservation for over five days
(now() + interval '5 days',  now() + interval '1 days', 0, 1, '001'),
-- One reservation for five days ago, attended to
(now() + interval '5 days',  now() + interval '3 days', 0, 1, '001');
