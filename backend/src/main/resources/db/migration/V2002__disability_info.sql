create
  table
  if not exists dis_info (
                           user_info_id bigint  not null,
                           ds_type      integer not null,
                           mu_rule      bytea        default null,
                           level        integer not null,
                           cert_code    varchar(255) default null,
                           cause        text         default null,
                           place        bytea        default null --

                         );

comment on
  table
  dis_info is '残障信息';

select add_base_struct('dis_info');

select ct_idx(
         'dis_info',
         'user_info_id'
       );

select ct_idx(
         'dis_info',
         'cert_code'
       );
