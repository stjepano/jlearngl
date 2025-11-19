package dev.stjepano.platform.opengl;

public enum GLPolygonRasterMode {
    POINT(0x1B00),
    LINE(0x1B01),
    FILL(0x1B02)
    ;
    private final int glEnumVal;

    GLPolygonRasterMode(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return glEnumVal;
    }
}
