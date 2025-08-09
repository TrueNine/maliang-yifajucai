alter table
  if exists enterprise
  add column if not exists capital_currency varchar(3) default null;
