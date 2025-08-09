alter table
  if exists enterprise
  add column if not exists data_score int default 0;

select ct_idx(
         'enterprise',
         'data_score'
       );
