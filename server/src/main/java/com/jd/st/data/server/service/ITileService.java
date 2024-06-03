package com.jd.st.data.server.service;


import com.jd.st.data.storage.tile.TileCoord;

public interface ITileService {

    byte[] getTile(TileCoord tileCoord);
}
