//
// Created by jesp on 2023-06-30.
//

#include "blur-manager.h"
#include "blur.h"
#include <android/log.h>

#define TAG "blur-manager.cpp"
#define ANDROID_LOG_DEBUG 3
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

void BlurManager::initBlur(JNIEnv *env, jobject thiz, jobject blur_manager, jint width, jint height, jint radius, jdouble resize_ratio) {
    LOGD("initBlur() called");
    blurManagerClass = env->GetObjectClass(blur_manager);
    blurManagerObject = env->NewGlobalRef(blur_manager);
    onBlurredMethodId = env->GetMethodID(blurManagerClass, "onBlurred", "(Landroid/graphics/Bitmap;)V");

    sharedValues = init(width, height, radius, resize_ratio);
}

SharedValues *BlurManager::getSharedValues() const {
    return sharedValues;
}

void BlurManager::startBlur(JNIEnv *env, jobject srcBitmap) const {
    LOGD("startBlur() called");

    AndroidBitmapInfo info;
    void *pixels = nullptr;

    if ((AndroidBitmap_getInfo(env, srcBitmap, &info)) < 0) return;
    if ((AndroidBitmap_lockPixels(env, srcBitmap, (void **) &pixels)) < 0) return;

    blur((short *) pixels, sharedValues);

    AndroidBitmap_unlockPixels(env, srcBitmap);

    sendBitmap(env, srcBitmap);
}

void BlurManager::sendBitmap(JNIEnv *env, jobject bitmap) const {
    LOGD("sendBitmap() called");
    env->CallVoidMethod(blurManagerObject, onBlurredMethodId, bitmap);
}

BlurManager *BlurManager::instance = nullptr;

BlurManager &BlurManager::getInstance() {
    if (instance == nullptr) instance = new BlurManager();
    return *instance;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_NativeImageProcessor_onDetachedFromWindow(JNIEnv *env, jobject thiz) {
    LOGD("onDetachedFromWindow() called");
    BlurManager &blurManager = BlurManager::getInstance();

    delete blurManager.sharedValues;
    delete blurManager.bitmapClass;

    env->DeleteGlobalRef(blurManager.blurManagerClass);
    env->DeleteGlobalRef(blurManager.blurManagerObject);
}
