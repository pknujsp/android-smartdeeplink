//
// Created by jesp on 2023-06-30.
//

#include "blur-manager.h"
#include "blur.h"

using namespace blurManager;

void initBlur(JNIEnv *env, jobject blur_manager, jint width, jint height, jint radius, jdouble resize_ratio) {
    blurManagerObject = blur_manager;
    blurManagerClass = env->GetObjectClass(blurManagerObject);

    jint srcBitmapWidth = width;
    jint srcBitmapHeight = height;

    jint targetBitmapWidth = width;
    jint targetBitmapHeight = height;

    if (resize_ratio > 1.0) {
        targetBitmapWidth = (jint) (srcBitmapWidth / resize_ratio);
        targetBitmapHeight = (jint) (srcBitmapHeight / resize_ratio);
    }

    if (targetBitmapWidth % 2 != 0) targetBitmapWidth--;
    if (targetBitmapHeight % 2 != 0) targetBitmapHeight--;

    sharedValues = init(targetBitmapWidth, targetBitmapHeight, radius, resize_ratio > 1.0);
}

void blur(JNIEnv *env, jobject srcBitmap) {
    void *pixels = nullptr;

    if ((AndroidBitmap_getInfo(env, srcBitmap, &info)) < 0) return;
    if ((AndroidBitmap_lockPixels(env, srcBitmap, (void **) &pixels)) < 0) return;

    blur((unsigned short *) &pixels, sharedValues);

    AndroidBitmap_unlockPixels(env, srcBitmap);
}
