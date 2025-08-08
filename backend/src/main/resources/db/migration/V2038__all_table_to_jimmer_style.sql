do
$$
    declare
        r record;

    begin
        for r in select tablename
                 from pg_tables
                 where schemaname = 'public'
                   and tablename = lower(tablename)
            loop
                execute format(
                    'select base_struct_to_jimmer_style(%L);',
                    r.tablename
                        );
            end loop;
    end
$$;
