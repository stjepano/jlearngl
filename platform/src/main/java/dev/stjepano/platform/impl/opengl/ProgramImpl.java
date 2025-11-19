package dev.stjepano.platform.impl.opengl;

import dev.stjepano.platform.opengl.Program;

final class ProgramImpl implements Program {
    /* package */ int glId;

    ProgramImpl(int glId) {
        this.glId = glId;
    }

    @Override
    public void delete() {
        JGL.jglDeleteProgram(glId);
    }

}
