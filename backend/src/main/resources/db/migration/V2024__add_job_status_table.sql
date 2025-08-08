create
    table
    if not exists job_seeker_status (
                                        remark           text      default null,
                                        amount           decimal(10,
                                                             2)    default null,
                                        audit_userid     bigint    default null,
                                        audit_status     integer   default null,
                                        created_datetime timestamp default now(),
                                        status           integer   default null,
                                        job_id           bigint not null,
                                        job_seeker_id    bigint    default null,
                                        user_id          bigint not null
                                    );

select add_base_struct('job_seeker_status');
