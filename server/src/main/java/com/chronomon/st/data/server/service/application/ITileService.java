package com.chronomon.st.data.server.service.application;


import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;

public interface ITileService {

    byte[] getTile(TileTemporalQueryParam param);
}
