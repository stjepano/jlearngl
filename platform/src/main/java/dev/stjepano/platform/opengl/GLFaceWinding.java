package dev.stjepano.platform.opengl;

public enum GLFaceWinding {
    CW(0x0900),
    CCW(0x0901);

    private final int glEnumVal;

    GLFaceWinding(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return glEnumVal;
    }
}
