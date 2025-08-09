create
  table
  if not exists black_list (
                             audit_status        integer   default null,
                             create_user_id      bigint    default null,
                             report_user_id      bigint    default null,
                             report_user_info_id bigint    default null,
                             black_ref_id        bigint    default null,
                             re_item_type        integer not null,
                             black_user_info_id  bigint    default null,
                             create_datetime     timestamp default null,
                             event_doc           text      default null
                           );

select add_base_struct('black_list');

select ct_idx(
         'black_list',
         'black_ref_id'
       );

select ct_idx(
         'black_list',
         'create_user_id'
       );

select ct_idx(
         'black_list',
         'report_user_id'
       );

select ct_idx(
         'black_list',
         'black_user_info_id'
       );

select ct_idx(
         'black_list',
         'report_user_info_id'
       );

create
  table
  if not exists black_list_tag (
                                 title varchar(127) not null,
                                 doc   text default null
                               );

select add_base_struct('black_list_tag');

select ct_idx(
         'black_list_tag',
         'title'
       );

create
  table
  if not exists black_list_black_list_tag (
                                            black_list_id bigint not null,
                                            tag_id        bigint not null
                                          );

select add_base_struct('black_list_black_list_tag');

select ct_idx(
         'black_list_black_list_tag',
         'black_list_id'
       );

select ct_idx(
         'black_list_black_list_tag',
         'tag_id'
       );

create
  table
  if not exists black_list_relation (
                                      ref_id        bigint default null,
                                      user_info_id  bigint default null,
                                      black_list_id bigint  not null,
                                      re_type       integer not null,
                                      re_item_type  integer not null,
                                      event_doc     text   default null
                                    );

select add_base_struct('black_list_relation');

select ct_idx(
         'black_list_relation',
         'ref_id'
       );

select ct_idx(
         'black_list_relation',
         'user_info_id'
       );

select ct_idx(
         'black_list_relation',
         'black_list_id'
       );

create
  table
  if not exists black_list_evidence (
                                      audit_status  integer      default null,
                                      title         varchar(127) default null,
                                      doc           text         default null,
                                      black_list_id bigint not null,
                                      att_id        bigint not null
                                    );

select add_base_struct('black_list_evidence');

select ct_idx(
         'black_list_evidence',
         'black_list_id'
       );

select ct_idx(
         'black_list_evidence',
         'att_id'
       );
