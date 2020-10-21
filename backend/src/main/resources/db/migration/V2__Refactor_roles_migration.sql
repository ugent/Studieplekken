-- First, add a column `admin` to USERS
ALTER TABLE public.users
ADD COLUMN admin boolean NOT NULL DEFAULT false;

ALTER TABLE public.users_to_verify
ADD COLUMN admin boolean NOT NULL DEFAULT false;

-- Set the admin column based on the current role of USERS
UPDATE public.users
SET admin = true
WHERE role LIKE '%ADMIN%';

UPDATE public.users_to_verify
SET admin = true
WHERE role LIKE '%ADMIN%';

-- Delete the column `role` in USERS
ALTER TABLE public.users
DROP COLUMN IF EXISTS role;

ALTER TABLE public.users_to_verify
DROP COLUMN IF EXISTS role;

-- Drop table ROLES
DROP TABLE IF EXISTS roles;
