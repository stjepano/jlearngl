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
typedef jfloat jGLfloat;


PLATFORM_API void jglViewport(jGLint x, jGLint y, jGLsizei w, jGLsizei h);
PLATFORM_API void jglClearNamedFramebufferiv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLint *value);
PLATFORM_API void jglClearNamedFramebufferuiv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLuint *value);
PLATFORM_API void jglClearNamedFramebufferfv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLfloat *value);
PLATFORM_API void jglClearNamedFramebufferfi(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, jGLfloat depth, jGLint stencil);


#endif //PLATFORM_GLWRAP_H