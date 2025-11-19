package dev.stjepano.platform.impl.opengl;

import dev.stjepano.platform.opengl.Buffer;
import dev.stjepano.platform.opengl.GLDataType;
import dev.stjepano.platform.opengl.OpenGLException;
import dev.stjepano.platform.opengl.VertexArray;

final class VertexArrayImpl implements VertexArray {
    /* package */ int glId;

    VertexArrayImpl(int glId) {
        this.glId = glId;
    }

    @Override
    public void vertexBuffer(int bindingIndex, Buffer buffer, long offset, int stride) {
        if (buffer instanceof BufferImpl bufferImpl) {
            if (!JGL.jglVertexArrayVertexBuffer(glId, bindingIndex, bufferImpl.glId, offset, stride)) {
                throw new OpenGLException("Failed to bind buffer to vertex array at binding index " + bindingIndex);
            }
        } else {
            throw new IllegalStateException("Buffer not instance of BufferImpl!");
        }
    }

    @Override
    public void vertexAttrib(int attribIndex, int bufferBindingIndex, int numComponents, GLDataType componentType, boolean normalized, int offset) {
        if (!JGL.jglVertexArrayVertexAttrib(glId, attribIndex, bufferBindingIndex, numComponents, componentType.glEnumValue(), normalized, offset)) {
            throw new OpenGLException("Failed to configure vertex attribute with index " + attribIndex + " for buffer at index " + bufferBindingIndex);
        }
    }

    @Override
    public void toggleAttrib(int attribIndex, boolean enabled) {
        if (!JGL.jglVertexArrayToggleAttrib(glId, attribIndex, enabled)) {
            throw new OpenGLException("Failed to " + (enabled ? "enable" : "disable") + " vertex attribute index " + attribIndex);
        }
    }

    @Override
    public void indexBuffer(Buffer indexBuffer) {
        if (indexBuffer instanceof BufferImpl bufferImpl) {
            if (!JGL.jglVertexArrayIndexBuffer(glId, bufferImpl.glId)) {
                throw new OpenGLException("Failed to bind index buffer to vertex array.");
            }
        } else {
            throw new IllegalStateException("indexBuffer not instance of BufferImpl");
        }

    }

    @Override
    public void delete() {
        JGL.jglDeleteVertexArray(glId);
    }
}
