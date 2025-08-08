alter table
    if exists enterprise
    alter reg_capital type decimal(20,
        2);

alter table
    if exists job
    alter min_salary type decimal(20,
        2),
    alter max_salary type decimal(20,
        2);

alter table
    if exists job_seeker
    alter ex_max_salary type decimal(20,
        2),
    alter ex_min_salary type decimal(20,
        2);
