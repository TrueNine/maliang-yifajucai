alter table
    if exists job_seeker alter column ex_industry_id drop
        not null,
        alter column ex_industry_id
    set
        default null,
        alter column ex_address_code drop
            not null,
            alter column ex_address_code
        set
            default null,
            alter column ex_remote drop
                not null,
                alter column ex_remote
            set
                default null,
                alter column rq_social drop
                    not null,
                    alter column rq_social
                set
                    default null,
                    alter column ex_industry_id drop
                        not null,
                        alter column ex_industry_id
                    set
                        default null,
                        alter column ex_min_salary drop
                            not null,
                            alter column ex_min_salary
                        set
                            default null,
                            alter column ex_min_salary type decimal(
                                20,
                                2
                            ),
                            alter column ex_max_salary drop
                                not null,
                                alter column ex_max_salary
                            set
                                default null,
                                alter column ex_max_salary type decimal(
                                    20,
                                    2
                                ),
                                add column if not exists rq_goto_work boolean default null;
