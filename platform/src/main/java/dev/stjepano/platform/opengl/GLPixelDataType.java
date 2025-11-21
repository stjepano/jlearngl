package dev.stjepano.platform.opengl;

public enum GLPixelDataType {
    BYTE(0x1400),
    UNSIGNED_BYTE(0x1401),
    SHORT(0x1402),
    UNSIGNED_SHORT(0x1403),
    INT(0x1404),
    UNSIGNED_INT(0x1405),
    FLOAT(0x1406),
    // there are others
    ;

    private final int glEnumVal;

    GLPixelDataType(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return this.glEnumVal;
    }
}
