create
    table
        if not exists rq_cert_rule(
            co_type integer default null,
            po_type integer default null,
            do_type integer default null,
            rq boolean default true,
            title varchar(255) default null,
            remark text default null
        );

comment on
table
    rq_cert_rule is '需求证件规则';

select
    add_base_struct('rq_cert_rule');

create
    table
        if not exists job_rq_cert_rule(
            job_id bigint not null,
            rq_cert_rule_id bigint not null
        );

comment on
table
    job_rq_cert_rule is '职位  证件规则';

select
    add_base_struct('job_rq_cert_rule');

select
    ct_idx(
        'job_rq_cert_rule',
        'job_id'
    );

select
    ct_idx(
        'job_rq_cert_rule',
        'rq_cert_rule_id'
    );

create
    table
        if not exists anonymous_cert_group(
            serial_no varchar(127) not null,
            up_user_id bigint not null,
            cert_id bigint not null
        );

comment on
table
    anonymous_cert_group is '匿名用户证件组';

select
    add_base_struct('anonymous_cert_group');

select
    ct_idx(
        'anonymous_cert_group',
        'serial_no'
    );

select
    ct_idx(
        'anonymous_cert_group',
        'up_user_id'
    );

select
    ct_idx(
        'anonymous_cert_group',
        'cert_id'
    );
