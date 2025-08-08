alter table
    if exists dis_info alter column user_info_id
set
    default null,
    alter column user_info_id drop
        not null;
