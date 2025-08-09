-- 添加 api 表的唯一约束以支持 postAllFound 方法的 upsert 操作

-- 删除可能存在的重复数据（保留最新的记录）
delete
from api
where ctid not in (select max(ctid)
                   from api
                   group by api_path, api_method);

-- 添加复合唯一约束
alter table api
  add constraint uk_api_path_method
    unique (api_path, api_method);
