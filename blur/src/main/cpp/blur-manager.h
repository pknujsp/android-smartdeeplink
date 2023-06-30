//
// Created by jesp on 2023-06-30.
//

#ifndef TESTBED_BLUR_MANAGER_H
#define TESTBED_BLUR_MANAGER_H

#include <jni.h>
#include "shared-values.h"
#include <android/bitmap.h>

class BlurManager {
private:

    BlurManager() {};
    static BlurManager *instance;

public:
    jclass bitmapClass = nullptr;
    jclass blurManagerClass = nullptr;
    jobject blurManagerObject = nullptr;

    AndroidBitmapInfo info;

    jmethodID createBitmapMethod = nullptr;
    jmethodID createScaledBitmapMethod = nullptr;
    jmethodID onWindowDetachListenerMethodId = nullptr;
    jmethodID onBlurredMethodId = nullptr;

    SharedValues *sharedValues = nullptr;

    // Singleton 객체를 얻는 함수
    static BlurManager &getInstance();

    // 복사를 막기 위한 코드
    BlurManager(BlurManager const &) = delete;  // 복사 생성자
    void operator=(BlurManager const &) = delete;  // 복사 대입 연산자

    void initBlur(JNIEnv *env, jobject thiz, jobject blur_manager, jint width, jint height, jint radius, jdouble resize_ratio);

    void startBlur(JNIEnv *env, jobject srcBitmap);

    SharedValues *getSharedValues() const;

    void sendBitmap(JNIEnv *env, jobject bitmap) const;
};

#endif //TESTBED_BLUR_MANAGER_H
