alter table
    if exists enterprise add column if not exists leader_name varchar(255) default null,
    add column if not exists reg_capital decimal(
        10,
        2
    ) default null,
    add column if not exists status int default null,
    add column if not exists reg_date date default null,
    add column if not exists credit_code_v1 varchar(1023) default null;

create
    table
        if not exists enterprise_phone(
            enterprise_id bigint not null,
            phone_type int default null,
            phone varchar(1023) not null
        );

select
    add_base_struct('enterprise_phone');

create
    table
        if not exists enterprise_email(
            enterprise_id bigint not null,
            email varchar(1023) not null
        );

select
    add_base_struct('enterprise_email');

select
    ct_idx(
        'enterprise_email',
        'enterprise_id'
    );

select
    ct_idx(
        'enterprise_email',
        'email'
    );
