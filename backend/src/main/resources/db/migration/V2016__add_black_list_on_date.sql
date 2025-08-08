alter table
    if exists black_list
    add column if not exists on_date date default null;
