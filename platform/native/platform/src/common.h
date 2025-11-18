#ifndef PLATFORM_COMMON_H
#define PLATFORM_COMMON_H

#define PLATFORM_API __attribute__((visibility("default")))

#include <stdint.h>

typedef int8_t jboolean;
typedef int8_t jbyte;
typedef int16_t jshort;
typedef int32_t jint;
typedef int32_t jint_result;
typedef int64_t jlong;
typedef float jfloat;
typedef double jdouble;

#define RESULT_OK 0
#define RESULT_FAIL 1

#define JTRUE 1
#define JFALSE 0

#endif //PLATFORM_COMMON_H