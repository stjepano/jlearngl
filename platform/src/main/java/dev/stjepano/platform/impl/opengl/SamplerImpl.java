package dev.stjepano.platform.impl.opengl;

import dev.stjepano.platform.memory.StackAllocator;
import dev.stjepano.platform.opengl.OpenGLException;
import dev.stjepano.platform.opengl.Sampler;
import dev.stjepano.platform.opengl.SamplerParameters;

import java.lang.foreign.MemorySegment;
import java.util.List;

class SamplerImpl implements Sampler  {
    /* package */ int glId;
    private SamplerParameters parameters;

    public SamplerImpl(int glId, SamplerParameters parameters) {
        this.glId = glId;
        this.parameters = parameters;
    }


    @Override
    public SamplerParameters parameters() {
        return parameters;
    }

    @Override
    public void configure(SamplerParameters parameters) {
        checkDeleted();

        List<OpenGLImpl.GLParameter> delta = OpenGLImpl.samplerParametersDelta(parameters, this.parameters);

        if (!delta.isEmpty()) {
            long deltaStreamSize = OpenGLImpl.calculateParametersStreamSize(delta);
            try (StackAllocator stack = StackAllocator.push()) {
                MemorySegment stream = stack.allocate(deltaStreamSize, 8);
                OpenGLImpl.encodeParametersStream(delta, stream);

                if (!JGL.jglSamplerConfigure(glId, stream, stream.byteSize())) {
                    throw new OpenGLException("Failed to configure sampler.");
                }
            }
        }
        this.parameters = parameters;

    }

    @Override
    public void delete() {
        if (this.glId != 0) {
            JGL.jglDeleteSampler(this.glId);
            this.glId = 0;
        }
    }

    private void checkDeleted() {
        if (glId == 0) {
            throw new IllegalStateException("Texture is deleted.");
        }
    }
}
