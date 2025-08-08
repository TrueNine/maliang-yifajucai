create
    table
        if not exists job_queue(
            job_id bigint not null,
            job_seeker_id bigint not null,
            user_id bigint default null,
            create_user_id bigint not null,
            user_info_id bigint default null,
            create_datetime bigint not null,
            cancel_datetime bigint default null,
            order_weight bigint not null
        );

select
    add_base_struct('job_queue');

select
    ct_idx(
        'job_queue',
        'job_id'
    );

select
    ct_idx(
        'job_queue',
        'user_id'
    );

select
    ct_idx(
        'job_queue',
        'user_info_id'
    );

select
    ct_idx(
        'job_queue',
        'job_seeker_id'
    );

select
    ct_idx(
        'job_queue',
        'create_user_id'
    );

select
    ct_idx(
        'job_queue',
        'order_weight'
    );
