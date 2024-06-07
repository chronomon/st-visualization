package com.chronomon.st.data.server.catalog;

/**
 * 动态表名处理器，给用户表名加上目录后缀
 *
 * @author wangrubin
 */
public class TableNameHandler implements com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler {

    /**
     * 用户表前缀
     */
    public static final String USER_TABLE_PREFIX = "t_user";

    @Override
    public String dynamicTableName(String sql, String tableName) {
        if (tableName.startsWith(USER_TABLE_PREFIX)) {
            // 对于用户表，表名字后缀为用户目录
            return tableName + "_" + CatalogContext.getCatalog().getAccessKey();
        } else {
            //对于系统表，表名没有后缀
            return tableName;
        }
    }
}
