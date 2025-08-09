create
  table
  if not exists job_seeker (
                             user_id          bigint    not null,
                             ex_address_code  bigint    not null,
                             ex_remote        boolean   not null,
                             rq_social        boolean   not null,
                             ex_industry_id   bigint    not null,
                             ex_min_salary    decimal(10,
                                                2)      not null,
                             ex_max_salary    decimal(10,
                                                2),
                             create_datetime  timestamp not null default now(),
                             reg_address_code varchar(255)       default null,
                             degree           integer            default null
                           );

comment on
  table
  job_seeker is '求职者';

select add_base_struct('job_seeker');

select ct_idx(
         'job_seeker',
         'user_id'
       );

select ct_idx(
         'job_seeker',
         'reg_address_code'
       );

select ct_idx(
         'job_seeker',
         'ex_address_code'
       );

create
  table
  if not exists work_experience (
                                  user_id          bigint not null,
                                  enterprise_id    bigint       default null,
                                  com_name         varchar(127) default null,
                                  com_address_code varchar(255) default null,
                                  start_date       date         default null,
                                  end_date         date         default null,
                                  title            varchar(127) default null,
                                  doc              text         default null
                                );

select add_base_struct('work_experience');

comment on
  table
  work_experience is '工作经历';

select ct_idx(
         'wrok_experience',
         'user_id'
       );

create
  table
  if not exists industry (
                           title varchar(127) unique not null,
                           doc   text default null
                         );

comment on
  table
  industry is '行业划分';

select add_presort_tree_struct('industry');

create
  table
  if not exists enterprise (
                             leader_user_id      bigint       default null,
                             leader_user_info_id bigint       default null,
                             address_details_id  bigint       default null,
                             address_code        varchar(255) default null,
                             enterprise_type     integer      default null,
                             industry_id         bigint       default null,
                             logo_attachment_id  bigint       default null,
                             employee_count      integer      default null,
                             title               varchar(255) default null,
                             doc                 text         default null,
                             create_datetime     timestamp    default now()
                           );

comment on
  table
  enterprise is '企业信息';

select add_presort_tree_struct('enterprise');

select add_base_struct('enterprise');

select ct_idx(
         'enterprise',
         'leader_user_id'
       );

select ct_idx(
         'enterprise',
         'address_details_id'
       );

select ct_idx(
         'enterprise',
         'address_code'
       );

select ct_idx(
         'enterprise',
         'industry_id'
       );

select ct_idx(
         'enterprise',
         'logo_attachment_id'
       );

create
  table
  if not exists enterprise_user (
                                  user_id       bigint not null,
                                  user_info_id  bigint not null,
                                  enterprise_id bigint not null,
                                  title         varchar(255) default null
                                );

comment on
  table
  enterprise_user is '企业用户信息';

select ct_idx(
         'enterprise_user',
         'user_id'
       );

select ct_idx(
         'enterprise_user',
         'enterprise_id'
       );

create
  table
  if not exists job (
                      rq_gender          integer      default null,
                      audit_status       integer      default 0,
                      rq_count           integer      default null,
                      enterprise_id      bigint       default null,
                      enterprise_user_id bigint       default null,
                      user_info_id       bigint       default null,
                      ordered_weight     integer      default 0,
                      industry_id        bigint       default null,
                      enterprise_type    integer      default null,
                      dispatch_type      integer      default null,
                      start_date         date         not null,
                      end_date           date         not null,
                      title              varchar(127) not null,
                      doc                text         not null,
                      qualification      text         default null,
                      post_resp          text         default null,
                      post_type          integer      default null,
                      rq_dis_rule        bytea        default null,
                      post_period        varchar(255) default null,
                      job_type           integer      default null,
                      ex_year            integer      default null,
                      min_salary         decimal(10,
                                           2)         default null,
                      max_salary         decimal(10,
                                           2)         default null,
                      max_man_age        integer      default null,
                      max_woman_age      integer      default null,
                      min_man_age        integer      default null,
                      min_woman_age      integer      default null,
                      contact_name       varchar(255) default null,
                      phone              varchar(63)  default null,
                      landline           varchar(255) default null,
                      address_details_id bigint       default null,
                      address_code       varchar(127) default null,
                      rq_address_code    varchar(255) default null,
                      create_datetime    timestamp    default now()
                    );

select add_base_struct('job');

comment on
  table
  job is '职位';

select ct_idx(
         'job',
         'enterprise_id'
       );

select ct_idx(
         'job',
         'address_details_id'
       );

select ct_idx(
         'job',
         'address_code'
       );

select ct_idx(
         'job',
         'rq_address_code'
       );

select ct_idx(
         'job',
         'rq_dis_rule'
       );

create
  table
  if not exists job_tag (
                          name varchar(127) not null,
                          doc  text default null
                        );

comment on
  table
  job_tag is '职位标签';

select add_base_struct('job_tag');

select ct_idx(
         'job_tag',
         'name'
       );

create
  table
  if not exists job_job_tag (
                              job_id     bigint not null,
                              job_tag_id bigint not null
                            );

comment on
  table
  job_job_tag is '职位 职位标签';

select add_base_struct('job_job_tag');

select ct_idx(
         'job_job_tag',
         'job_id'
       );

select ct_idx(
         'job_job_tag',
         'job_tag_id'
       );

create
  table
  if not exists job_rule_address (
                                   job_id       bigint not null,
                                   rule_type    integer      default null,
                                   address_code varchar(127) default null,
                                   address_id   varchar(127) default null
                                 );

select ct_idx(
         'job_rule_address',
         'job_id'
       );

comment on
  table
  job_rule_address is '职位地址规则';

select add_base_struct('job_rule_address');
