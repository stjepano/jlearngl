package dev.stjepano.platform.opengl;

public enum GLCompareFunc {
    LESS(0x0201),
    LEQUAL(0x0203),
    EQUAL(0x0202),
    GREATER(0x0204),
    GEQUAL(0x0206),
    NOTEQUAL(0x0205),
    ALWAYS(0x0207),
    NEVER(0x0200)
    ;

    private final int glEnumVal;

    GLCompareFunc(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return glEnumVal;
    }
}
