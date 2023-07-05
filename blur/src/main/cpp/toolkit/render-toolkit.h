//
// Created by jesp on 2023-07-05.
//

#ifndef TESTBED_RENDER_TOOLKIT_H
#define TESTBED_RENDER_TOOLKIT_H

#include <jni.h>
#include <android/bitmap.h>

static jobject prepareBlur(JNIEnv *env, jobject src, jint radius);

static void blur(jobject src, jobject dst);

#endif //TESTBED_RENDER_TOOLKIT_H
