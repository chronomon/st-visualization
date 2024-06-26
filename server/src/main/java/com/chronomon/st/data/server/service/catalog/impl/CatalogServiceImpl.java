package com.chronomon.st.data.server.service.catalog.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronomon.st.data.server.compnent.CatalogHolder;
import com.chronomon.st.data.server.dao.CatalogMapper;
import com.chronomon.st.data.server.model.vo.CatalogVO;
import com.chronomon.st.data.server.service.catalog.ICatalogService;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.service.data.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
public class CatalogServiceImpl extends ServiceImpl<CatalogMapper, CatalogPO> implements ICatalogService {

    @Resource
    private IRawDataService rawDataService;

    @Resource
    private ITileBatchService tileBatchService;

    @Resource
    private ITileStatisticService tileStatisticService;

    @Resource
    private IOidBatchService oidBatchService;

    @Resource
    private IOidStatisticService oidStatisticService;

    @Resource
    private CatalogHolder catalogHolder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CatalogPO initCatalog(CatalogVO param) {
        if (catalogExists(param.getCatalogName())) {
            throw new IllegalArgumentException("用户目录已存在：" + param.getCatalogName());
        }

        // 创建用户目录实体
        param.setPeriodUnit(param.getPeriodUnit().toUpperCase());
        ChronoUnit periodUnit = ChronoUnit.valueOf(param.getPeriodUnit());
        String catalogId = UUID.randomUUID().toString().replaceAll("-", "");
        CatalogPO catalogPO = new CatalogPO();
        BeanUtils.copyProperties(param, catalogPO);
        catalogPO.setCatalogId(catalogId);
        // 下一个需要归档的时间片默认为纪元时间
        catalogPO.setNextRollPeriod(Instant.ofEpochSecond(0).truncatedTo(periodUnit).getEpochSecond());
        catalogPO.setCreateTime(LocalDateTime.now());
        save(catalogPO);

        // 创建用户目录对应的数据表
        rawDataService.createTable(catalogId);
        tileBatchService.createTable(catalogId);
        oidBatchService.createTable(catalogId);
        tileStatisticService.createTable(catalogId);
        oidStatisticService.createTable(catalogId);

        return catalogPO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean destroyCatalogById(String catalogId) {
        CatalogPO catalogPO = getCatalogById(catalogId);
        if (catalogPO != null) {
            // 删除用户目录
            removeById(catalogPO.getId());

            // 删除数据表
            rawDataService.dropTable(catalogId);
            tileBatchService.dropTable(catalogId);
            oidBatchService.dropTable(catalogId);
            tileStatisticService.dropTable(catalogId);
            oidStatisticService.dropTable(catalogId);

            // 删除缓存
            catalogHolder.cleanCache(catalogId);

            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean destroyCatalogByName(String catalogName) {
        LambdaQueryWrapper<CatalogPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CatalogPO::getCatalogName, catalogName);
        CatalogPO catalogPO = getOne(queryWrapper, false);
        if (catalogPO == null) {
            return false;
        }
        return destroyCatalogById(catalogPO.getCatalogId());
    }

    @Override
    public boolean updateCatalog(CatalogPO catalogPO) {
        // 删除缓存
        catalogHolder.cleanCache(catalogPO.getCatalogId());
        return updateById(catalogPO);
    }


    @Override
    public boolean catalogExists(String catalogName) {
        LambdaQueryWrapper<CatalogPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CatalogPO::getCatalogName, catalogName);
        return count(queryWrapper) > 0;
    }

    @Override
    public CatalogPO getCatalogById(String catalogId) {
        LambdaQueryWrapper<CatalogPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CatalogPO::getCatalogId, catalogId);
        return getOne(queryWrapper, false);
    }

    @Override
    public CatalogPO getCatalogByName(String catalogName) {
        LambdaQueryWrapper<CatalogPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CatalogPO::getCatalogName, catalogName);
        return getOne(queryWrapper, false);
    }
}
