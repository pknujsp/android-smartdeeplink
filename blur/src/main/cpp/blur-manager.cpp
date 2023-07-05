//
// Created by jesp on 2023-06-30.
//

#include "blur-manager.h"
#include "blur.h"
#include <android/log.h>

#define TAG "blur-manager.cpp"
#define ANDROID_LOG_DEBUG 3
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

void BlurManager::initBlur(JNIEnv *env, jobject thiz, jint width, jint height, jint radius, jdouble resize_ratio) {
    sharedValues = init(width, height, radius, resize_ratio);
}

SharedValues *BlurManager::getSharedValues() const {
    return sharedValues;
}

jobject BlurManager::startBlur(JNIEnv *env, jobject srcBitmap) const {
    void *pixels = nullptr;

    if ((AndroidBitmap_lockPixels(env, srcBitmap, (void **) &pixels)) < 0) return nullptr;
    blur((short *) pixels, sharedValues);
    AndroidBitmap_unlockPixels(env, srcBitmap);
    return srcBitmap;
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
