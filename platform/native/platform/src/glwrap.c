#include "glwrap.h"

void jglViewport(jGLint x, jGLint y, jGLsizei w, jGLsizei h) {
    glViewport((GLint) x, (GLint) y, (GLsizei) w, (GLsizei) h);
}

void jglClearNamedFramebufferiv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLint *value) {
    glClearNamedFramebufferiv((GLuint) framebuffer, (GLenum) buffer, (GLint) drawbuffer, (const GLint*) value);
}

void jglClearNamedFramebufferuiv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLuint *value) {
    glClearNamedFramebufferuiv((GLuint) framebuffer, (GLenum) buffer, (GLint) drawbuffer, (const GLuint*) value);
}

void jglClearNamedFramebufferfv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLfloat *value) {
    glClearNamedFramebufferfv((GLuint) framebuffer, (GLenum) buffer, (GLint) drawbuffer, (const GLfloat*) value);
}

void jglClearNamedFramebufferfi(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, jGLfloat depth, jGLint stencil) {
    glClearNamedFramebufferfi((GLuint) framebuffer, (GLenum) buffer, (GLint) drawbuffer, (GLfloat) depth, (GLint) stencil);
}

jboolean jglNamedBufferSubData(jGLuint buffer, jGLintptr offset, jGLsizeiptr size, const void *data) {
    glNamedBufferSubData((GLuint) buffer, (GLintptr) offset, (GLsizeiptr) size, data);
    return glGetError() == GL_NO_ERROR;
}

void * jglMapNamedBufferRange(jGLuint buffer, jGLintptr offset, jGLsizeiptr length, jGLbitfield accessflags) {
    return glMapNamedBufferRange((GLuint) buffer, (GLintptr) offset, (GLsizeiptr) length, (GLbitfield) accessflags);
}

jboolean jglUnmapNamedBuffer(jGLuint buffer) {
    glUnmapNamedBuffer((GLuint) buffer);
    return glGetError() == GL_NO_ERROR;
}

jboolean jglFlushMappedNamedBufferRange(jGLuint buffer, jGLintptr offset, jGLsizeiptr length) {
    glFlushMappedNamedBufferRange((GLuint) buffer, (GLintptr) offset, (GLsizeiptr) length);
    return glGetError() == GL_NO_ERROR;
}

void jglDeleteBuffers(jGLuint buffer) {
    GLuint buf = buffer;
    glDeleteBuffers(1, &buf);
}

GLuint jglCreateBufferWithStorage(jGLsizeiptr byteSize, jGLbitfield storageflags, const void *data) {
    GLuint buffer = 0;
    glCreateBuffers(1, &buffer);
    if (buffer) {
        glNamedBufferStorage(buffer, (GLsizeiptr) byteSize, data, (GLbitfield) storageflags);
        if (glGetError() != GL_NO_ERROR) {
            glDeleteBuffers(1, &buffer);
            buffer = 0;
        }
    }
    return buffer;
}
