package dev.stjepano.platform.opengl;

public enum GLDataType {
    BYTE(1, 0x1400),
    UNSIGNED_BYTE(1, 0x1401),
    SHORT(2, 0x1402),
    UNSIGNED_SHORT(2, 0x1403),
    INT(4, 0x1404),
    UNSIGNED_INT(4, 0x1405),
    FLOAT(4, 0x1406),
    HALF_FLOAT(2, 0x140B),
    DOUBLE(8, 0x140A);

    private final long byteSize;
    private final int glEnumVal;

    GLDataType(long byteSize, int glEnumVal) {
        this.byteSize = byteSize;
        this.glEnumVal = glEnumVal;
    }

    public long byteSize() {
        return byteSize;
    }

    public int glEnumValue() {
        return glEnumVal;
    }
}
