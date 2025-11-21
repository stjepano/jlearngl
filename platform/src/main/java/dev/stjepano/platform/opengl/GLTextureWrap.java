package dev.stjepano.platform.opengl;

public enum GLTextureWrap {
    REPEAT(0x2901),
    CLAMP_TO_EDGE(0x812F),
    CLAMP_TO_BORDER(0x812D),
    MIRRORED_REPEAT(0x8370)
    ;

    private final int glEnumVal;

    GLTextureWrap(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return this.glEnumVal;
    }
}
