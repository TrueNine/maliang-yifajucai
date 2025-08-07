alter table
    job add column if not exists degree integer default null,
    add column if not exists ready_status integer default null;

create
    unique index if not exists user_id_unique on
    job_seeker(user_id);

alter table
    if exists job_seeker add column if not exists employment_state integer default null;
