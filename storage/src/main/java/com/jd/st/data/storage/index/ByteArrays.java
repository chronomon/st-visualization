package com.jd.st.data.storage.index;

public class ByteArrays {
    /**
     * Writes the byte as 1 byte in the provided array, starting at offset
     *
     * @param value  short to write
     * @param bytes  byte array to write to, must have length at least `offset` + 2
     * @param offset offset to start writing
     */
    public static void writeByte(byte value, byte[] bytes, int offset) {
        bytes[offset] = value;
    }

    /**
     * Writes the short as 2 bytes in the provided array, starting at offset
     *
     * @param value  short to write
     * @param bytes  byte array to write to, must have length at least `offset` + 2
     * @param offset offset to start writing
     */
    public static void writeShort(short value, byte[] bytes, int offset) {
        bytes[offset] = (byte) (value >> 8);
        bytes[offset + 1] = (byte) value;
    }

    /**
     * Writes the int as 4 bytes in the provided array, starting at offset
     *
     * @param value  int to write
     * @param bytes  byte array to write to, must have length at least `offset` + 8
     * @param offset offset to start writing
     */
    public static void writeInt(int value, byte[] bytes, int offset) {
        bytes[offset] = (byte) ((value >> 24) & 0xff);
        bytes[offset + 1] = (byte) ((value >> 16) & 0xff);
        bytes[offset + 2] = (byte) ((value >> 8) & 0xff);
        bytes[offset + 3] = (byte) (value & 0xff);
    }

    /**
     * Writes the long as 8 bytes in the provided array, starting at offset
     *
     * @param value  long to write
     * @param bytes  byte array to write to, must have length at least `offset` + 8
     * @param offset offset to start writing
     */
    public static void writeLong(long value, byte[] bytes, int offset) {
        bytes[offset] = (byte) ((value >> 56) & 0xff);
        bytes[offset + 1] = (byte) ((value >> 48) & 0xff);
        bytes[offset + 2] = (byte) ((value >> 40) & 0xff);
        bytes[offset + 3] = (byte) ((value >> 32) & 0xff);
        bytes[offset + 4] = (byte) ((value >> 24) & 0xff);
        bytes[offset + 5] = (byte) ((value >> 16) & 0xff);
        bytes[offset + 6] = (byte) ((value >> 8) & 0xff);
        bytes[offset + 7] = (byte) (value & 0xff);
    }

    /**
     * Reads 2 bytes from the provided array as a short, starting at offset
     *
     * @param bytes  array to read from
     * @param offset offset to start reading
     * @return
     */
    public static short readShort(byte[] bytes, int offset) {
        return (short) (((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff));
    }


    /**
     * Reads 4 bytes from the provided array as an int, starting at offset
     *
     * @param bytes  array to read from
     * @param offset offset to start reading
     * @return
     */
    public static int readInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xff) << 24) |
                ((bytes[offset + 1] & 0xff) << 16) |
                ((bytes[offset + 2] & 0xff) << 8) |
                (bytes[offset + 3] & 0xff);
    }

    /**
     * Reads 8 bytes from the provided array as a long, starting at offset
     *
     * @param bytes  array to read from
     * @param offset offset to start reading
     * @return
     */
    public static long readLong(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xffL) << 56) |
                ((bytes[offset + 1] & 0xffL) << 48) |
                ((bytes[offset + 2] & 0xffL) << 40) |
                ((bytes[offset + 3] & 0xffL) << 32) |
                ((bytes[offset + 4] & 0xffL) << 24) |
                ((bytes[offset + 5] & 0xffL) << 16) |
                ((bytes[offset + 6] & 0xffL) << 8) |
                (bytes[offset + 7] & 0xffL);
    }
}
