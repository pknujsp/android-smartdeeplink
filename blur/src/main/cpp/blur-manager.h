//
// Created by jesp on 2023-06-30.
//

#ifndef TESTBED_BLUR_MANAGER_H
#define TESTBED_BLUR_MANAGER_H

#include <jni.h>
#include "shared-values.h"
#include <android/bitmap.h>

namespace blurManager {
    static jclass bitmapClass;
    static jclass blurManagerClass;
    static jobject blurManagerObject;
    static AndroidBitmapInfo info;

    static jmethodID createBitmapMethod;
    static jmethodID createScaledBitmapMethod;

    static SharedValues *sharedValues;

    void blur(JNIEnv *env, jobject srcBitmap);

    void initBlur(JNIEnv *env, jobject blur_manager, jobject thiz, jint width, jint height, jint radius, jdouble resize_ratio);
}

#endif //TESTBED_BLUR_MANAGER_H
