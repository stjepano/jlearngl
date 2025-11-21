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
PLATFORM_API jboolean jglPolygonMode(jGLenum mode);

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

PLATFORM_API void jglProgramUniform1i(jGLuint program_id, jGLint location, jGLint i);
PLATFORM_API void jglProgramUniform1iv(jGLuint program_id, jGLint location, jGLsizei count, const jGLint* value_ptr);
PLATFORM_API void jglProgramUniform1f(jGLuint program_id, jGLint location, jGLfloat x);
PLATFORM_API void jglProgramUniform1fv(jGLuint program_id, jGLint location, jGLsizei count, const jGLfloat* value_ptr);
PLATFORM_API void jglProgramUniform2f(jGLuint program_id, jGLint location, jGLfloat x, jGLfloat y);
PLATFORM_API void jglProgramUniform2fv(jGLuint program_id, jGLint location, jGLsizei count, const jGLfloat* value_ptr);
PLATFORM_API void jglProgramUniform3f(jGLuint program_id, jGLint location, jGLfloat x, jGLfloat y, jGLfloat z);
PLATFORM_API void jglProgramUniform3fv(jGLuint program_id, jGLint location, jGLsizei count, const jGLfloat* value_ptr);
PLATFORM_API void jglProgramUniform4f(jGLuint program_id, jGLint location, jGLfloat x, jGLfloat y, jGLfloat z, jGLfloat w);
PLATFORM_API void jglProgramUniform4fv(jGLuint program_id, jGLint location, jGLsizei count, const jGLfloat* value_ptr);
PLATFORM_API void jglProgramUniformMatrix2fv(jGLuint program_id, jGLint location, jGLsizei count, jboolean transpose, const jGLfloat* value_ptr);
PLATFORM_API void jglProgramUniformMatrix3fv(jGLuint program_id, jGLint location, jGLsizei count, jboolean transpose, const jGLfloat* value_ptr);
PLATFORM_API void jglProgramUniformMatrix4fv(jGLuint program_id, jGLint location, jGLsizei count, jboolean transpose, const jGLfloat* value_ptr);

PLATFORM_API void jglDeleteProgram(jGLuint program_id);

PLATFORM_API jGLuint jglCreateVertexArray();
PLATFORM_API void jglBindVertexArray(jGLuint vao_id);
PLATFORM_API jboolean jglVertexArrayVertexBuffer(jGLuint vao_id, jGLuint binding_index, jGLuint buffer_id, jGLintptr offset, jGLsizei stride);
PLATFORM_API jboolean jglVertexArrayIndexBuffer(jGLuint vao_id, jGLuint buffer_id);
PLATFORM_API jboolean jglVertexArrayVertexAttrib(jGLuint vao_id, jGLuint attrib_index, jGLuint buffer_binding_index, jGLint num_component,
                                   jGLenum component_type, jboolean normalized, jGLuint offset);
PLATFORM_API jboolean jglVertexArrayToggleAttrib(jGLuint vao_id, jGLuint attrib_index, jboolean enabled);
PLATFORM_API void jglDeleteVertexArray(jGLuint vao_id);


PLATFORM_API jGLuint jglCreateTexture2D(jGLint mipLevels, jGLenum internalFormat, jGLsizei width, jGLsizei height, const void* parameterStream, jlong parameterStreamSize);
PLATFORM_API jboolean jglTextureUpdateSubImage2D(jGLuint textureId, jGLint mipLevel,
                                   jGLint x, jGLint y, jGLsizei width, jGLsizei height,
                                   jGLenum format, jGLenum type,
                                   jGLint rowAlignment, jGLint rowLength,
                                   const void* pixels);
PLATFORM_API jboolean jglTextureUpdateSubImage2DBuffer(jGLuint textureId, jGLint mipLevel,
                                   jGLint x, jGLint y, jGLsizei width, jGLsizei height,
                                   jGLenum format, jGLenum type,
                                   jGLint rowAlignment, jGLint rowLength,
                                   jGLuint bufferId, jGLsizeiptr offset);
PLATFORM_API void jglTextureGenerateMipmaps(jGLuint textureId);
PLATFORM_API jboolean jglTextureConfigure(jGLuint textureId, const void* parameterStream, jlong parameterStreamSize);
PLATFORM_API void jglDeleteTexture(jGLuint textureId);
PLATFORM_API void jglBindTextureUnit(jGLuint unit, jGLuint textureId);

PLATFORM_API void jglDrawArrays(jGLenum mode, jGLint first, jGLsizei count);
PLATFORM_API void jglDrawElements(jGLenum mode, jGLsizei count, jGLenum type, jlong offset);


#endif //PLATFORM_GLWRAP_H