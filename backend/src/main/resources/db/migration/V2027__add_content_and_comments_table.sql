create
    table
    if not exists contents (
                               title            varchar(255)           not null,
                               doc              varchar(511) default null,
                               pub_user_id      bigint                 not null,
                               to_user_id       bigint       default null,
                               pub_ip           varchar(63)  default null,
                               pub_address_code varchar(255) default null,
                               contents_type    int          default 0 not null,
                               audit_status     int          default 2,
                               text_content     text         default null,
                               create_datetime  timestamp    default now()
                           );

select add_base_struct('contents');

select add_tree_struct('contents');

select ct_idx(
           'contents',
           'pub_user_id'
       );

select ct_idx(
           'contents',
           'to_user_id'
       );

select ct_idx(
           'contents',
           'pub_ip'
       );

select ct_idx(
           'contents',
           'pub_address_code'
       );

create
    table
    if not exists contents_attachment (
                                          attachment_id bigint default null,
                                          contents_id   bigint default null
                                      );

select add_base_struct('contents_attachment');

select ct_idx(
           'contents_attachment',
           'attachment_id'
       );

select ct_idx(
           'contents_attachment',
           'contents_id'
       );

create
    table
    if not exists content_comments_votes (
                                             content_comments_id bigint not null,
                                             user_id             bigint not null,
                                             audit_status        int       default 2,
                                             vote_type           int    not null,
                                             to_type             int    not null,
                                             create_datetime     timestamp default now()
                                         );

select add_base_struct('content_comments_votes');

select ct_idx(
           'content_comments_votes',
           'content_comments_id'
       );

select ct_idx(
           'content_comments_votes',
           'user_id'
       );

create
    table
    if not exists contents_tags (
                                    title varchar(255) not null,
                                    doc   text default null
                                );

select add_base_struct('contents_tags');

create
    table
    if not exists contents_contents_tags (
                                             contents_id      bigint not null,
                                             contents_tags_id bigint not null
                                         );

select add_base_struct('contents_contents_tags');

select ct_idx(
           'contents_contents_tags',
           'contents_id'
       );

select ct_idx(
           'contents_contents_tags',
           'contents_tags_id'
       );
