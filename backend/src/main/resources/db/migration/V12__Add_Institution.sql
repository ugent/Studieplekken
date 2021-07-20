--
-- Add column 'institution' to table 'buildings'.
--
ALTER TABLE public.buildings ADD institution text;

ALTER TABLE public.buildings ADD
    constraint fk_buildings_to_institutions
        foreign key (institution)
            references public.institutions (name)
            on delete cascade
            on update cascade;

-- All buildings currently in the database are UGent buildings.
UPDATE public.buildings SET institution = 'UGent';