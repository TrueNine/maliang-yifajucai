do
$$
    begin -- 检查并重命名 audit_userid
        if exists(select 1
                  from information_schema.columns
                  where table_name = 'job_seeker_status'
                    and column_name = 'audit_userid') then
            execute 'alter table job_seeker_status rename column audit_userid to audit_user_id;';
        end if;

        if exists(select 1
                  from information_schema.columns
                  where table_name = 'job_seeker_status'
                    and column_name = 'audit_userId') then
            execute 'alter table job_seeker_status rename column audit_userId to audit_user_id;';
        end if;
    end
$$;
