alter table
    if exists dis_info
    alter column ds_type
        set
        default null,
    alter column ds_type drop
        not null,
    alter column level
        set
        default null,
    alter column level drop
        not null;
