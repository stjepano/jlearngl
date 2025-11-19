package dev.stjepano.platform.opengl;

public enum GLPrimitive {
    POINTS(0x0000),
    LINE_STRIP(0x0003),
    LINE_LOOP(0x0002),
    LINES(0x0001),
    LINE_STRIP_ADJACENCY(0x000B),
    LINES_ADJACENCY(0x000A),
    TRIANGLE_STRIP(0x0005),
    TRIANGLE_FAN(0x0006),
    TRIANGLES(0x0004),
    TRIANGLE_STRIP_ADJACENCY(0x000D),
    TRIANGLES_ADJACENCY(0x000C);

    private final int glEnumVal;

    GLPrimitive(int glEnumVal) {
        this.glEnumVal = glEnumVal;
    }

    public int glEnumValue() {
        return this.glEnumVal;
    }
}
