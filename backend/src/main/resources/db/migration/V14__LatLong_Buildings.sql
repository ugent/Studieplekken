--
-- Add latitude and longitude to buildings table.
--
ALTER TABLE public.buildings ADD latitude DOUBLE PRECISION NOT NULL DEFAULT 0;
ALTER TABLE public.buildings ADD longitude DOUBLE PRECISION NOT NULL DEFAULT 0;
