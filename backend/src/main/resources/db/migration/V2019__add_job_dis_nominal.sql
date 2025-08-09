create
  table
  if not exists job_dis_nominal (
                                  job_id                   bigint not null,
                                  task_on_month_count      integer      default null,
                                  task_on_year_count       integer      default null,
                                  contract_period          varchar(127) default null,
                                  has_offline_interview    boolean      default null,
                                  offline_interview_amount decimal(20,
                                                             2)         default null,
                                  has_renew_contract       boolean      default null,
                                  exception_out            boolean      default null,
                                  not_low_income_support   boolean      default null,
                                  not_social_support       boolean      default null,
                                  not_medical_support      boolean      default null
                                );

select add_base_struct('job_dis_nominal');
