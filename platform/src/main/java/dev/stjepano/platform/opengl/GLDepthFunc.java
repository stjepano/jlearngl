package dev.stjepano.platform.opengl;

public enum GLDepthFunc {
    NEVER(0x0200),
    LESS(0x0201),
    EQUAL(0x0202),
    LEQUAL(0x0203),
    GREATER(0x0204),
    NOTEQUAL(0x0205),
    GEQUAL(0x0206),
    ALWAYS(0x0207);

    private final int glEnumVal;

    GLDepthFunc(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return this.glEnumVal;
    }
}
