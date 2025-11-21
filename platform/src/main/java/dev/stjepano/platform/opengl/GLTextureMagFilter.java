package dev.stjepano.platform.opengl;

public enum GLTextureMagFilter {
    NEAREST(0x2600),
    LINEAR(0x2601);

    private final int glEnumVal;

    GLTextureMagFilter(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return glEnumVal;
    }
}
