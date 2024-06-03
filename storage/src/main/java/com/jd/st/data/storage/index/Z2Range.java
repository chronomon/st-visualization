package com.jd.st.data.storage.index;

public class Z2Range {

    public final long minZ2;

    public final long maxZ2;

    public Z2Range(long minZ2, long maxZ2) {
        assert minZ2 <= maxZ2 : "Z2 range is illegal";
        this.minZ2 = minZ2;
        this.maxZ2 = maxZ2;
    }

    public boolean contains(long z2){
       return  z2 >= minZ2 && z2 <= maxZ2;
    }

    public String toString() {
        return String.format("%s-%s", minZ2, maxZ2);
    }
}
