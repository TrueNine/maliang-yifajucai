create
    table
    if not exists enterprise_industry (
                                          industry_id   bigint not null,
                                          enterprise_id bigint not null
                                      );

select add_base_struct('enterprise_industry');
