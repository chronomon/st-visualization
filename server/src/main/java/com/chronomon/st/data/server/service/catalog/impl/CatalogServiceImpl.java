package com.chronomon.st.data.server.service.catalog.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronomon.st.data.server.dao.CatalogMapper;
import com.chronomon.st.data.server.service.catalog.ICatalogService;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CatalogServiceImpl extends ServiceImpl<CatalogMapper, CatalogPO> implements ICatalogService {
    @Override
    public CatalogPO getByAccessKey(String accessKey) {
        LambdaQueryWrapper<CatalogPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CatalogPO::getAccessKey, accessKey);
        return getOne(queryWrapper, false);
    }
}
