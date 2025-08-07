create
    table
        if not exists employment_record(
            typ boolean not null,
            job_id bigint not null,
            job_seeker_id bigint not null,
            user_id bigint not null,
            cause_by text default null,
            opt_user_id bigint default null,
            create_datetime timestamp default null,
            end_datetime timestamp default null
        );

select
    add_base_struct('employment_record');

select
    ct_idx(
        'employment_record',
        'job_id'
    );

select
    ct_idx(
        'employment_record',
        'job_seeker_id'
    );

select
    ct_idx(
        'employment_record',
        'user_id'
    );

select
    ct_idx(
        'employment_record',
        'opt_user_id'
    );

create
    table
        if not exists reg_share(
            share_enterprise_id bigint default null,
            share_user_id bigint not null,
            reg_user_id bigint default null,
            share_datetime timestamp not null,
            reg_datetime timestamp default null,
            reg_status boolean default null,
            share_end_datetime timestamp default now(),
            share_code varchar(255) unique not null
        );

select
    add_base_struct('reg_share');

select
    ct_idx(
        'reg_share',
        'share_user_id'
    );

select
    ct_idx(
        'reg_share',
        'reg_user_id'
    );

select
    ct_idx(
        'reg_share',
        'share_enterprise_id'
    );

create
    table
        if not exists job_seeker_superior(
            superior_user_id bigint not null,
            subordinate_user_id bigint not null,
            create_datetime timestamp not null,
            end_datetime timestamp default null
        );

select
    add_base_struct('job_seeker_superior');

select
    ct_idx(
        'job_seeker_superior',
        'superior_user_id'
    );

select
    ct_idx(
        'job_seeker_superior',
        'subordinate_user_id'
    );
