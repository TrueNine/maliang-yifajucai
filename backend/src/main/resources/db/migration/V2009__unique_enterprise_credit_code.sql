alter table
  if exists enterprise
  drop constraint if exists credit_code_v1_unique,
  add constraint credit_code_v1_unique unique (credit_code_v1);
