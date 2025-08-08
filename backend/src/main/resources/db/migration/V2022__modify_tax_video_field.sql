alter table
    if exists job_seeker_dis_nominal_tax_video add column if not exists problem boolean default null,
    add column if not exists problem_desc text default null,
    add column if not exists complaint text default null,
    add column if not exists audit_status int default null,
    add column if not exists complaint_audit_status int default null,
    add column if not exists audit_user_id bigint default null,
    add column if not exists audit_datetime timestamp default null,
    add column if not exists complaint_audit_user_id bigint default null,
    add column if not exists complaint_audit_datetime timestamp default null;

alter table
    if exists job_seeker_dis_nominal_tax_video alter column query_datetime drop
        not null,
        alter column query_datetime type timestamp,
        alter column query_datetime
    set
        default null;
