alter table
  if exists bank_card
  add column if not exists phone varchar(128) default null;
