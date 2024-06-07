-- 创建用户目录表
create table if not exists t_sys_catalog
(
    id               serial         primary key,
    catalog_name     varchar(255)   not null,
    access_key       varchar(64)    not null,
    max_zoom_level   smallint       not null,
    tile_extent      smallint       not null,
    period_unit      char(10)       not null,
    last_roll_time   timestamp      null,
    create_time      timestamp      not null
);
comment on table t_sys_catalog is '用户目录表';
comment on column t_sys_catalog.id is '自增主键';
comment on column t_sys_catalog.catalog_name is '用户目录名';
comment on column t_sys_catalog.access_key is '数据访问Key';
comment on column t_sys_catalog.max_zoom_level is '金字塔模型做大层级，[0,20]之间的整数';
comment on column t_sys_catalog.tile_extent is '地图瓦片边长(单位：像素)，2的幂次方';
comment on column t_sys_catalog.period_unit is '时间片单位，粒度从小到大有：Minutes、Hours、HalfDays、Days';
comment on column t_sys_catalog.last_roll_time is '最近一次归档的时间片结束时刻';
comment on column t_sys_catalog.create_time is '创建时间';
create index if not exists t_sys_catalog_access_key_idx on t_sys_catalog (access_key);

-- 创建原始数据表模板
create table if not exists t_sys_gps_raw_template
(
    id                 serial primary key,
    oid                varchar(255) not null,
    z_val              bigint       not null,
    period_start_time  timestamp       not null,
    tile_x             bigint       not null,
    tile_y             bigint       not null,
    time_offset        int    not null
);
comment on table t_sys_gps_raw_template is '地址空间表';
comment on column t_sys_gps_raw_template.id is '自增主键';
comment on column t_sys_gps_raw_template.oid is '对象ID';
comment on column t_sys_gps_raw_template.z_val is 'Z填充曲线编码值';
comment on column t_sys_gps_raw_template.tile_x is '瓦片横坐标(单位：像素)';
comment on column t_sys_gps_raw_template.tile_y is '瓦片纵坐标(单位：像素)';
comment on column t_sys_gps_raw_template.time_offset is '时间片内的秒数';
create index if not exists t_sys_gps_raw_template_oid_period_start_time_idx on t_sys_gps_raw_template (oid, period_start_time);
create index if not exists t_sys_gps_raw_template_z_val_period_start_time_idx on t_sys_gps_raw_template (z_val, period_start_time);

-- 创建数据包表模板
create table if not exists t_sys_gps_batch_template
(
    combine_index       varchar(255)       primary key,
    data_batch          bytea
);
comment on table t_sys_gps_batch_template is '数据包表模板';
comment on column t_sys_gps_batch_template.combine_index is '组合索引，做为主键';
comment on column t_sys_gps_batch_template.data_batch is '压缩后的数据包';

-- 创建统计信息表模板
create table if not exists t_sys_gps_statistic_template
(
    period_start_time   bigint     primary key,
    data_batch          bytea
);
comment on table t_sys_gps_statistic_template is '统计信息表模板';
comment on column t_sys_gps_statistic_template.period_start_time is '时间片起始时刻';
comment on column t_sys_gps_statistic_template.data_batch is '压缩后的统计数据';