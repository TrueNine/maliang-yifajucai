create
    table
    if not exists bank (
                           group_type varchar(255)        not null,
                           bank_name  varchar(255) unique not null,
                           region     varchar(255)        not null
                       );

select add_base_struct('bank');

do
$$
    begin
        if not exists(select 1
                      from bank) then
            insert
            into bank(id,
                      group_type,
                      bank_name,
                      region)
            values (1,
                    'UNI_PAY',
                    '中国银行',
                    'CNY'),
                   (2,
                    'UNI_PAY',
                    '中国工商银行',
                    'CNY'),
                   (3,
                    'UNI_PAY',
                    '中国农业银行',
                    'CNY'),
                   (4,
                    'UNI_PAY',
                    '中国建设银行',
                    'CNY'),
                   (5,
                    'UNI_PAY',
                    '交通银行',
                    'CNY'),
                   (6,
                    'UNI_PAY',
                    '招商银行',
                    'CNY'),
                   (7,
                    'UNI_PAY',
                    '中信银行',
                    'CNY'),
                   (8,
                    'UNI_PAY',
                    '兴业银行',
                    'CNY'),
                   (9,
                    'UNI_PAY',
                    '浦发银行',
                    'CNY'),
                   (10,
                    'UNI_PAY',
                    '光大银行',
                    'CNY'),
                   (11,
                    'UNI_PAY',
                    '华夏银行',
                    'CNY'),
                   (12,
                    'UNI_PAY',
                    '平安银行',
                    'CNY'),
                   (13,
                    'UNI_PAY',
                    '民生银行',
                    'CNY'),
                   (14,
                    'UNI_PAY',
                    '广发银行',
                    'CNY');
        end if;
    end
$$;
