alter table
    if exists job
    add column if not exists payday                 integer default 15,
    add column if not exists salary_commission_rule varchar(255),
    add column if not exists subsidy                varchar(255);
