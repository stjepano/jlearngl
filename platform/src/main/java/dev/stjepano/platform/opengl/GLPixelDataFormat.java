package dev.stjepano.platform.opengl;

public enum GLPixelDataFormat {
    RED(0x1903),
    RG(0x8227),
    RGB(0x1907),
    BGR(0x80E0),
    RGBA(0x1908),
    BGRA(0x80E1),
    DEPTH_COMPONENT(0x1902),
    STENCIL_INDEX(0x1901)
    ;

    private final int glEnumVal;

    GLPixelDataFormat(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return glEnumVal;
    }
}
