package com.chronomon.st.data.server.service.data.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DynamicTableService {
    @Resource
    private JdbcTemplate jdbcTemplate;

    public void createTable(String tableName, String templateName) {
        jdbcTemplate.execute("CREATE TABLE " + tableName + " (LIKE " + templateName + " INCLUDING ALL);");
    }

    public void dropTable(String tableName) {
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName + " ;");
    }
}
