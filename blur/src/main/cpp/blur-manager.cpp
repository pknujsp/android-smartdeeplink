//
// Created by jesp on 2023-06-30.
//

#include "blur-manager.h"
#include "blur.h"
#include <android/log.h>

#define TAG "blur-manager.cpp"
#define ANDROID_LOG_DEBUG 3
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

void BlurManager::initBlur(JNIEnv *env, jobject thiz, jobject blur_listener, jint width, jint height, jint radius, jdouble resize_ratio) {
    blurListenerObject = env->NewGlobalRef(blur_listener);
    onBlurredMethodId = env->GetMethodID(env->GetObjectClass(blur_listener), "onBlurred", "(Landroid/graphics/Bitmap;)V");

    sharedValues = init(width, height, radius, resize_ratio);
}

SharedValues *BlurManager::getSharedValues() const {
    return sharedValues;
}

void BlurManager::startBlur(JNIEnv *env, jobject srcBitmap) const {
    void *pixels = nullptr;

    if ((AndroidBitmap_lockPixels(env, srcBitmap, (void **) &pixels)) < 0) return;

    blur((short *) pixels, sharedValues);

    AndroidBitmap_unlockPixels(env, srcBitmap);

    sendBitmap(env, srcBitmap);
}

void BlurManager::sendBitmap(JNIEnv *env, jobject bitmap) const {
    env->CallVoidMethod(blurListenerObject, onBlurredMethodId, bitmap);
}

BlurManager *BlurManager::instance = nullptr;

BlurManager &BlurManager::getInstance() {
    if (instance == nullptr) instance = new BlurManager();
    return *instance;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_natives_NativeImageProcessorImpl_onClear(JNIEnv *env, jobject thiz) {
    BlurManager &blurManager = BlurManager::getInstance();

    delete blurManager.sharedValues;
    delete blurManager.bitmapClass;
}
