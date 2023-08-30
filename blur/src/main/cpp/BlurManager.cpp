//
// Created by jesp on 2023-07-05.
//

#include "BlurManager.h"
#include "stackblur/abgr-stackblur.h"
#include <jni.h>
#include <android/bitmap.h>
#include <android/native_window.h>
#include <android/hardware_buffer.h>
#include <android/hardware_buffer_jni.h>
#include <android/window.h>
#include <android/surface_texture.h>
#include <android/surface_texture_jni.h>

static ABGRStackBlur *stackBlur = new ABGRStackBlur();

extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_natives_NativeImageProcessorImpl_prepareBlur(JNIEnv *env, jobject thiz, jint width, jint height, jint radius,
                                                                         jdouble resize_ratio) {
    stackBlur->prepare(width, height, radius, resize_ratio);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_io_github_pknujsp_blur_natives_NativeImageProcessorImpl_blur(JNIEnv *env, jobject thiz, jobject src_bitmap) {
    void *pixels;
    AndroidBitmap_lockPixels(env, src_bitmap, &pixels);
    stackBlur->blur((unsigned int *) pixels);
    AndroidBitmap_unlockPixels(env, src_bitmap);
    return src_bitmap;
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_natives_NativeImageProcessorImpl_onClear(JNIEnv *env, jobject thiz) {
    stackBlur->onDestroy();
}
