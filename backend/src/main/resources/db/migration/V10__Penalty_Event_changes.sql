--
-- 1. Alter columns of penalty_events: add description_dutch and description_english, and add sequencer to code
-- 2. Set the columns for the existing penalty events
-- 3. Delete penalty_descriptions and corresponding trigger
-- 4. Delete languages and corresponding trigger
-- 5. The type of column timeslot of penalty_book changed from text to timestamp
--

-- 1
alter table public.penalty_events
add column description_dutch text,
add column description_english text;

create sequence if not exists penalty_events_code_seq;

alter table public.penalty_events
alter column code set default nextval('penalty_events_code_seq');

-- 2
with x as (
    select pd.event_code, pd.description as en, pd2.description as nl
    from public.penalty_descriptions pd
        join public.penalty_descriptions pd2
            on pd2.event_code = pd.event_code
    where pd.lang_enum = 'ENGLISH' and pd2.lang_enum = 'DUTCH'
)
update public.penalty_events pe
set description_dutch = nl, description_english = x.en
from x
where pe.code = x.event_code;

-- 3
drop trigger set_timestamp_penalty_descriptions
on public.penalty_descriptions;

drop table public.penalty_descriptions;

-- 4
drop trigger set_timestamp_languages
on public.languages;

drop table public.languages;

-- 5
alter table public.penalty_book
alter column timestamp set data type timestamp
using "timestamp"::timestamp without time zone;
