package dev.stjepano.platform.opengl;

public enum GLTextureMinFilter {
    NEAREST(0x2600),
    LINEAR(0x2601),
    NEAREST_MIPMAP_NEAREST(0x2700),
    LINEAR_MIPMAP_NEAREST(0x2701),
    NEAREST_MIPMAP_LINEAR(0x2702),
    LINEAR_MIPMAP_LINEAR(0x2703),
    ;

    private final int glEnumVal;

    GLTextureMinFilter(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return glEnumVal;
    }
}
