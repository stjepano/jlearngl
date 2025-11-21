package dev.stjepano.platform.opengl;

public enum GLTextureCompareMode {
    NONE(0),
    COMPARE_REF_TO_TEXTURE(0x884E)

    ;

    private final int glEnumVal;

    GLTextureCompareMode(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return glEnumVal;
    }
}
