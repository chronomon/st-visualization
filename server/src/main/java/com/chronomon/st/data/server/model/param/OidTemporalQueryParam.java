package com.chronomon.st.data.server.model.param;

import lombok.Data;

/**
 * OID + 时间范围查询参数
 *
 * @author wangrubin
 */
@Data
public class OidTemporalQueryParam extends TemporalParam {

    private String oid;
}
