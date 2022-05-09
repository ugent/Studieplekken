DROP TABLE IF EXISTS public.user_settings;
CREATE TABLE public.user_settings
    (
             user_id                    TEXT    PRIMARY KEY,
             receive_mail_confirmation  BOOLEAN NOT NULL DEFAULT TRUE,
             CONSTRAINT fk_user_settings_user
                 FOREIGN KEY(user_id)
            REFERENCES public.users(user_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
    );


-- Create user settings for all users that currently exist
INSERT INTO public.user_settings (user_id)
    SELECT user_id FROM public.users;



-- Automatically create the user settings
-- whenever a new user is added into the system.
CREATE OR REPLACE FUNCTION f_create_user_settings()
    RETURNS TRIGGER AS $$
    BEGIN
        INSERT INTO user_settings (user_id)
        VALUES (NEW.user_id);
        RETURN NEW;
    END;
    $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS create_user_settings_trigger ON users;
CREATE TRIGGER create_user_settings_trigger
        AFTER INSERT ON users
    FOR EACH ROW
    EXECUTE PROCEDURE f_create_user_settings();

ALTER TABLE LOCATIONS
    ADD reminder_dutch      TEXT    NOT NULL    DEFAULT '';

ALTER TABLE LOCATIONS
    ADD reminder_english    TEXT    NOT NULL    DEFAULT '';