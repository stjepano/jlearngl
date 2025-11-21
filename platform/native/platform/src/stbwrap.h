#ifndef PLATFORM_STBWRAP_H
#define PLATFORM_STBWRAP_H

#include "common.h"

typedef struct {
    int width;
    int height;
    int channels;
} ImageInfo;

PLATFORM_API void* jstbiLoadFromFile(const char* filePathPtr, jboolean flipVertically, ImageInfo* outImageInfoPtr, char* outErrorBuf, jint outErrorBufSize);
PLATFORM_API void* jstbiLoadFromMemory(const void* memory, jlong memorySize, jboolean flipVertically, ImageInfo* outImageInfoPtr, char* outErrorBuf, jint outErrorBufSize);
PLATFORM_API void jstbiFree(void* memory);

#endif //PLATFORM_STBWRAP_H