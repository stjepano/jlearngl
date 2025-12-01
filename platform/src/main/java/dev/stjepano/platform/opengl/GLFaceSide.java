package dev.stjepano.platform.opengl;

public enum GLFaceSide {
    FRONT(0x0404),
    BACK(0x0405),
    FRONT_AND_BACK(0x0408);

    private final int glEnumVal;

    GLFaceSide(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return glEnumVal;
    }
}
