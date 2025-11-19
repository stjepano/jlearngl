#ifndef PLATFORM_GLWRAP_H
#define PLATFORM_GLWRAP_H

#include "common.h"
#include <glad/glad.h>

typedef jboolean jGLboolean;
typedef jbyte jGLbyte;
typedef jbyte jGLubyte;
typedef jshort jGLshort;
typedef jshort jGLushort;
typedef jint jGLsizei;
typedef jint jGLint;
typedef jint jGLuint;
typedef jint jGLenum;
typedef jint jGLbitfield;
typedef jlong jGLintptr;
typedef jlong jGLsizeiptr;
typedef jfloat jGLfloat;


PLATFORM_API void jglViewport(jGLint x, jGLint y, jGLsizei w, jGLsizei h);
PLATFORM_API void jglClearNamedFramebufferiv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLint *value);
PLATFORM_API void jglClearNamedFramebufferuiv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLuint *value);
PLATFORM_API void jglClearNamedFramebufferfv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLfloat *value);
PLATFORM_API void jglClearNamedFramebufferfi(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, jGLfloat depth, jGLint stencil);

PLATFORM_API jboolean jglNamedBufferSubData(jGLuint buffer, jGLintptr offset, jGLsizeiptr size, const void *data);
PLATFORM_API void *jglMapNamedBufferRange(jGLuint buffer, jGLintptr offset, jGLsizeiptr length, jGLbitfield accessflags);
PLATFORM_API jboolean jglUnmapNamedBuffer(jGLuint buffer);
PLATFORM_API jboolean jglFlushMappedNamedBufferRange(jGLuint buffer, jGLintptr offset, jGLsizeiptr length);
PLATFORM_API void jglDeleteBuffers(jGLuint buffer);
PLATFORM_API GLuint jglCreateBufferWithStorage(jGLsizeiptr byteSize, jGLbitfield storageflags, const void* data);


#endif //PLATFORM_GLWRAP_H