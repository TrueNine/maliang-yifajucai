create
    table
    if not exists job_seeker_dis_nominal (
                                             job_seeker_id          bigint unique not null,
                                             has_low_income_support boolean default null,
                                             has_social_support     boolean default null,
                                             has_medical_support    boolean default null,
                                             acc_offline_interview  boolean default null,
                                             acc_collateral_money   boolean default null,
                                             acc_collateral_cert    boolean default null
                                         );

select add_base_struct('job_seeker_dis_nominal');

create
    table
    if not exists job_seeker_dis_nominal_tax_video (
                                                       job_seeker_id  bigint    not null,
                                                       att_id         bigint    not null,
                                                       query_datetime timestamp not null
                                                   );

select add_base_struct('job_seeker_dis_nominal_tax_video');
