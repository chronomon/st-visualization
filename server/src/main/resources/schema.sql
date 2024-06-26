-- 创建用户目录表
create table if not exists t_sys_catalog
(
    id               serial         primary key,
    catalog_name     varchar(255)   not null,
    catalog_id       varchar(64)    not null,
    max_zoom_level   smallint       not null,
    tile_extent      smallint       not null,
    period_unit      varchar(10)       not null,
    next_roll_period bigint         null,
    create_time      timestamp      not null
);
comment on table t_sys_catalog is '用户目录表';
comment on column t_sys_catalog.id is '自增主键';
comment on column t_sys_catalog.catalog_name is '用户目录名';
comment on column t_sys_catalog.catalog_id is '用户目录ID(UUID)';
comment on column t_sys_catalog.max_zoom_level is '金字塔模型做大层级，[0,20]之间的整数';
comment on column t_sys_catalog.tile_extent is '地图瓦片边长(单位：像素)，2的幂次方';
comment on column t_sys_catalog.period_unit is '时间片单位，粒度从小到大有：Minutes、Hours、HalfDays、Days';
comment on column t_sys_catalog.next_roll_period is '下一次待归档的时间片';
comment on column t_sys_catalog.create_time is '创建时间';
create index if not exists t_sys_catalog_catalog_id_idx on t_sys_catalog (catalog_id);
create index if not exists t_sys_catalog_catalog_name_idx on t_sys_catalog (catalog_name);

-- 创建原始数据表模板
create table if not exists t_template_gps_raw
(
    id                 serial primary key,
    oid                varchar(255) not null,
    z_val              bigint       not null,
    map_x              bigint       not null,
    map_y              bigint       not null,
    time               bigint    not null
);
comment on table t_template_gps_raw is '地址空间表';
comment on column t_template_gps_raw.id is '自增主键';
comment on column t_template_gps_raw.oid is '对象ID';
comment on column t_template_gps_raw.z_val is 'Z填充曲线编码值';
comment on column t_template_gps_raw.map_x is '地图横坐标(单位：像素)';
comment on column t_template_gps_raw.map_y is '地图纵坐标(单位：像素)';
comment on column t_template_gps_raw.time is '时间';
create index if not exists t_template_gps_raw_oid_idx on t_template_gps_raw (oid);
create index if not exists t_template_gps_raw_z_val_idx on t_template_gps_raw (z_val);

-- 创建数据包表模板
create table if not exists t_template_gps_batch
(
    combine_index       varchar(255)       primary key,
    data_batch          bytea
);
comment on table t_template_gps_batch is '数据包表模板';
comment on column t_template_gps_batch.combine_index is '组合索引，做为主键';
comment on column t_template_gps_batch.data_batch is '压缩后的数据包';

-- 创建统计信息表模板
create table if not exists t_template_gps_statistic
(
    period_start_time   bigint     primary key,
    data_batch          bytea
);
comment on table t_template_gps_statistic is '统计信息表模板';
comment on column t_template_gps_statistic.period_start_time is '时间片起始时刻';
comment on column t_template_gps_statistic.data_batch is '压缩后的统计数据';