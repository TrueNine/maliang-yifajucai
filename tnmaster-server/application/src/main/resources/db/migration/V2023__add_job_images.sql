create
    table
        if not exists job_details_img(
            img_att_id bigint not null,
            job_id bigint not null
        );

select
    add_base_struct('job_details_img');
