alter table
  if exists job
  alter column doc type text,
  alter column doc
    set
    default null,
  alter column qualification type text,
  alter column qualification
    set
    default null;
