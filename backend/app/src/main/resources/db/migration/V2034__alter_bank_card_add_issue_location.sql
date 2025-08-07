alter table
    if exists bank_card add column if not exists issue_location varchar(255) default null;
