
-- Initial seeding of a database with data

/*
 * Setup users
 */
INSERT into users(augentid, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password, institution, admin)
VALUES ('000170763345', 'Bram', 'Van de Walle', 0, 'bram.vandewalle@ugent.be', 'secret', 'UGent', true);

INSERT into users(augentid, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password, institution, admin)
VALUES ('000150595025', 'Ruben', 'De Facq', 0, 'Ruben.DeFacq@UGent.be ', 'secret', 'UGent', true);

INSERT into users(augentid, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password, institution, admin)
VALUES ('000160298358', 'Maxime', 'Bloch', 0, 'maxime.bloch@ugent.be', 'secret', 'UGent', true);

INSERT into users(augentid, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password, institution, admin)
VALUES ('000170335535', 'Maxiem', 'Geldhof', 0, ' Maxiem.Geldhof@UGent.be ', 'secret', 'UGent', true);

/*
 * Setup third test user
 */
insert into public.users(augentid, augentpreferredgivenname, augentpreferredsn, penalty_points, mail, password, institution, admin)
values ('01405190', 'Tim', 'Van Erum', 0, 'tim.vanerum@ugent.be', 'secret', 'UGent', true);

/*
 * Setup two test locations with an authority and add 2nd test user to the authority
 */
DO $$
DECLARE auth_id integer;
DECLARE build_id_therminal integer;

BEGIN
  INSERT INTO public.authority (authority_name, description) values ('DSA', 'Dienst StudentenActiviteiten') RETURNING authority_id into auth_id;
  INSERT INTO public.buildings (building_name, address) VALUES ('Therminal', 'Hoveniersberg 24, 9000 Gent') RETURNING building_id into build_id_therminal;
  INSERT INTO public.locations (name, building_id, number_of_seats, number_of_lockers, image_url, authority_id, description_dutch, description_english, forGroup, approved)
    VALUES  ('Turbinezaal', build_id_therminal, 50, 100, '', auth_id, 'neder', 'engl', false, false),
                ('Plenaire vergaderzaal', build_id_therminal, 30, 0, '', auth_id, '', '', true, true),
                ('Podiumzaal', build_id_therminal, 100, 0, '', auth_id, '', '', false, true),
                ('Trechterzaal', build_id_therminal, 80, 0, '', auth_id, '', '', false, true);
  INSERT INTO public.roles_user_authority (user_id, authority_id) VALUES ('002', auth_id);
END $$;


DO $$
DECLARE new_authority_id integer;
DECLARE build_id_sterreS5 integer;
DECLARE build_id_sterreS9 integer;
DECLARE loc_id_S9_PC integer;
DECLARE loc_id_S5_bib integer;
DECLARE loc_id_S5_eetzaal integer;
DECLARE new_tag_id integer;

BEGIN
  INSERT INTO authority (authority_name, description) VALUES ('WE', 'Faculteit wetenschappen') RETURNING authority_id into new_authority_id;
  INSERT INTO public.buildings (building_name, address) VALUES ('Sterre S5', 'Krijgslaan 281, 9000 Gent') RETURNING building_id into build_id_sterreS5;
  INSERT INTO public.buildings (building_name, address) VALUES ('Sterre S9', 'Krijgslaan 281, 9000 Gent') RETURNING building_id into build_id_sterreS9;
  INSERT INTO locations (name, building_id, number_of_seats, number_of_lockers, image_url, authority_id, description_dutch, description_english, forGroup, approved)
    VALUES  ('Sterre S9, PC lokaal 3rd verdiep', build_id_sterreS9, 40, 100, '', new_authority_id, 'Klaslokaal met computers', 'Classroom with computers', false, true) RETURNING location_id INTO loc_id_S9_PC;
  INSERT INTO locations (name, building_id, number_of_seats, number_of_lockers, image_url, authority_id, description_dutch, description_english, forGroup, approved)
    VALUES  ('Sterre S5, Bib', build_id_sterreS5, 100, 100, '', new_authority_id,
                    'Informatie over de bib kan hier gevonden worden: https://lib.ugent.be/nl/libraries/WEBIB.',
                    'Information about the bib itself can be found here: https://lib.ugent.be/nl/libraries/WEBIB.', false, true) RETURNING location_id INTO loc_id_S5_bib;
  INSERT INTO locations (name, building_id, number_of_seats, number_of_lockers, image_url, authority_id, description_dutch, description_english, forGroup, approved)
    VALUES  ('Sterre S5, Eetzaal', build_id_sterreS5, 130, 100, '', new_authority_id, '', '', false, true) RETURNING location_id INTO loc_id_S5_eetzaal;
  INSERT INTO roles_user_authority (user_id, authority_id) VALUES ('002', new_authority_id);


  /*
  * add tags and use them in location Therminal
  */

  INSERT INTO tags (dutch, english) values ('eetplaats', 'dinner place') RETURNING tag_id into new_tag_id;
  INSERT INTO location_tags (location_id, tag_id) values (loc_id_S5_eetzaal, new_tag_id);

  INSERT INTO tags (dutch, english) values ('stilte', 'silencium') RETURNING tag_id into new_tag_id;
  INSERT INTO location_tags (location_id, tag_id) values (loc_id_S5_bib, new_tag_id);

  INSERT INTO tags (dutch, english) values ('computers', 'computers') RETURNING tag_id into new_tag_id;
  INSERT INTO location_tags (location_id, tag_id) values (loc_id_S9_PC, new_tag_id);


  /*
  * Add some calendar periods
  */
  insert into calendar_periods(location_id, starts_at, ends_at, opening_time, closing_time, reservable_from, reservable, timeslot_length, locked_from)
  values  (loc_id_S5_eetzaal, now() - interval '1 days', now() + interval '3 days',
              '10:00', '12:00', now() - interval '7 days', true, 60, now() + interval '1 week'),
          (loc_id_S5_bib, now() - interval '5 days', now() + interval '10 days',
              '09:00', '17:00', now() - interval '7 days', false, 0, now() + interval '1 week'),
          (loc_id_S9_PC,now() - interval '5 days', now() + interval '10 days',
              '8:30', '18:30', now() - interval '7 days', false, 0, now() + interval '1 week');

END$$;

insert into timeslots(calendar_id, timeslot_sequence_number, timeslot_date)
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
