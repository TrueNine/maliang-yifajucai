create
    table
        if not exists cert_access(
            meta boolean default false,
            access_ip varchar(255) default null,
            access_device_id varchar(255) default null,
            user_id bigint default null,
            wm_code varchar(255) default null,
            access_datetime timestamp default null,
            cert_id bigint default null,
            wm_att_id bigint default null,
            att_id bigint default null
        );

comment on
table
    cert_access is '证件调阅记录';

select
    add_base_struct('cert_access');

select
    ct_idx(
        'cert_access',
        'user_id'
    );

select
    ct_idx(
        'cert_access',
        'wm_code'
    );

select
    ct_idx(
        'cert_access',
        'cert_id'
    );

select
    ct_idx(
        'cert_access',
        'wm_att_id'
    );
