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
