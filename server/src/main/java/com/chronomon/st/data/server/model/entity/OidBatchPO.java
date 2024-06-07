package com.chronomon.st.data.server.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统表：用户目录元数据
 *
 * @author wangrubin
 */
@Data
@TableName("t_user_gps_oid_batch")
public class OidBatchPO {
    /**
     * OID+时间索引: oid + periodStartSeconds
     */
    @TableId(value = "combineIndex", type = IdType.NONE)
    private String combineIndex;

    /**
     * 压缩后的数据包
     */
    private byte[] dataBatch;
}
