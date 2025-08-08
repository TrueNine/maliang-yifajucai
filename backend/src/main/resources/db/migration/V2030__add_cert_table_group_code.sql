alter table
    if exists cert
    add column if not exists group_code varchar(255) default null,
    add column if not exists visible    boolean      default null;
