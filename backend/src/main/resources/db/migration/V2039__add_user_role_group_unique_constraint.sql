-- 添加 user_role_group 表的唯一约束以支持 ON CONFLICT 语句

-- 删除可能存在的重复数据
delete
from user_role_group
where ctid not in (select min(ctid)
                   from user_role_group
                   group by user_id, role_group_id);

-- 添加复合唯一约束
alter table user_role_group
  add constraint uk_user_role_group_user_id_role_group_id
    unique (user_id, role_group_id);
