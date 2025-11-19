#ifndef PLATFORM_GLWRAP_H
#define PLATFORM_GLWRAP_H

#include "common.h"

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
PLATFORM_API jGLuint jglCreateBufferWithStorage(jGLsizeiptr byteSize, jGLbitfield storageflags, const void* data);

PLATFORM_API jGLuint jglCreateProgramVF(const char* vertex_shader_src, const char* fragment_shader_src, char* error_buf, jlong error_buf_size);
PLATFORM_API void jglBindProgram(jGLuint program_id);
PLATFORM_API void jglDeleteProgram(jGLuint program_id);

PLATFORM_API jGLuint jglCreateVertexArray();
PLATFORM_API void jglBindVertexArray(jGLuint vao_id);
PLATFORM_API jboolean jglVertexArrayVertexBuffer(jGLuint vao_id, jGLuint binding_index, jGLuint buffer_id, jGLintptr offset, jGLsizei stride);
PLATFORM_API jboolean jglVertexArrayVertexAttrib(jGLuint vao_id, jGLuint attrib_index, jGLuint buffer_binding_index, jGLint num_component,
                                   jGLenum component_type, jboolean normalized, jGLuint offset);
PLATFORM_API jboolean jglVertexArrayToggleAttrib(jGLuint vao_id, jGLuint attrib_index, jboolean enabled);
PLATFORM_API void jglDeleteVertexArray(jGLuint vao_id);
PLATFORM_API void jglDrawArrays(jGLenum mode, jGLint first, jGLsizei count);


#endif //PLATFORM_GLWRAP_H