package com.chronomon.st.data.server.model.param;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OID + 时间范围查询参数
 *
 * @author wangrubin
 */
@Getter
@Setter
@NoArgsConstructor
public class OidTemporalQueryParam extends TemporalParam {

    private String oid;
}
