create
    table
        if not exists bank_card(
            code varchar(255) default null,
            bank_name varchar(255) default null,
            region varchar(255) default null,
            order_weight int default 0,
            available boolean default true,
            bank_group_name varchar(255) default null,
            user_account_id bigint not null
        );

select
    add_base_struct('bank_card');

create
    table
        if not exists bank_card_cert(
            cert_id bigint default null,
            bank_card_id bigint default null
        );

alter table
    if exists bank_card add column if not exists visible boolean default null;
