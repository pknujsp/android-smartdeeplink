//
// Created by jesp on 2023-06-30.
//

#include "blur-manager.h"
#include "blur.h"

using namespace blurManager;

static jmethodID onWindowDetachListenerMethodId;
static jmethodID onBlurredMethodId;

void ::blurManager::initBlur(JNIEnv *env, jobject thiz, jobject blur_manager, jint width, jint height, jint radius, jdouble resize_ratio) {
    blurManagerClass = env->GetObjectClass(blur_manager);
    blurManagerObject = env->NewGlobalRef(blur_manager);

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

    onWindowDetachListenerMethodId = env->GetMethodID(blurManagerClass, "onWindowDetachListener", "()V");
    onBlurredMethodId = env->GetMethodID(blurManagerClass, "onBlurred", "(Landroid/graphics/Bitmap;)V");

    // fun onBlurred(bitmap: Bitmap)
    // override var onWindowAttachListener: ViewTreeObserver.OnWindowAttachListener? = null
    // override var onWindowDetachListener: ViewTreeObserver.OnWindowAttachListener? = null
}

void sendBitmap(JNIEnv *env, jobject bitmap) {
    env->CallVoidMethod(blurManagerObject, onBlurredMethodId, bitmap);
}

void ::blurManager::blur(JNIEnv *env, jobject srcBitmap) {
    void *pixels = nullptr;

    if ((AndroidBitmap_getInfo(env, srcBitmap, &info)) < 0) return;
    if ((AndroidBitmap_lockPixels(env, srcBitmap, (void **) &pixels)) < 0) return;

    blur((unsigned short *) &pixels, sharedValues);

    AndroidBitmap_unlockPixels(env, srcBitmap);

    sendBitmap(env, srcBitmap);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_NativeImageProcessor_onDetachedFromWindow(JNIEnv *env, jobject thiz) {
    delete sharedValues;
    delete bitmapClass;

    env->DeleteGlobalRef(blurManagerClass);
    env->DeleteGlobalRef(blurManagerObject);
}
