package dev.stjepano.platform.opengl;

/// OpenGL internal sized texture formats.
public enum GLTexture2DFormat {
    R8(0x8229),
    R8_SNORM(0x8F94),
    RGB8(0x8051),
    RGB8_SNORM(0x8F96),
    RGBA8(0x8058),
    RGBA8_SNORM(0x8F97),
    R32F(0x822E),
    RGB32F(0x8815),
    RGBA32F(0x8814);

    private final int glEnumVal;

    GLTexture2DFormat(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return glEnumVal;
    }
}
