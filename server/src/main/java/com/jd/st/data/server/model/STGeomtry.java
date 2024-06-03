package com.jd.st.data.server.model;

import org.locationtech.jts.geom.Polygon;

public class STGeomtry {
    public final long id;

    public final long count;
    public final Polygon geom;

    public STGeomtry(long id, long count, Polygon geom) {
        this.id = id;
        this.count = count;
        this.geom = geom;
    }
}
