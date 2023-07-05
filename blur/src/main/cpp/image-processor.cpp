#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/window.h>
#include <functional>
#include <GLES3/gl31.h>
#include <GLES3/gl3ext.h>
#include "blur.h"
#include "blur-manager.h"
#include <mutex>

#define TAG "NativeImageProcessor"
#define ANDROID_LOG_DEBUG 3
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

std::mutex mutex;

void gl(unsigned int *pixels, const int width, const int height) {
    GLuint textureId;
    glGenTextures(1, &textureId);

// Bind the texture
    glBindTexture(GL_TEXTURE_2D, textureId);

// Set the texture parameters
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

// Upload the pixel data to the texture
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, pixels);

// Unbind the texture
    glBindTexture(GL_TEXTURE_2D, 0);
}

extern "C"
jobject resize(JNIEnv *env, jint newWidth, jint newHeight, _jobject *bitmap) {
    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");

    jmethodID createScaledBitmapMethod = env->GetStaticMethodID(bitmapClass, "createScaledBitmap",
                                                                "(Landroid/graphics/Bitmap;IIZ)Landroid/graphics/Bitmap;");

    bitmap = env->CallStaticObjectMethod(bitmapClass, createScaledBitmapMethod, bitmap, newWidth,
                                         newHeight, true);
    return bitmap;
}

extern "C"
jobject toBitmap(JNIEnv *env, jobject decorView, _jclass *decorViewClass, _jclass *bitmapClass, jint decorViewWidth, jint decorViewHeight,
                 jint statusBarHeight, jint navigationBarHeight) {
    jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                          "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    jclass configClass = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID configField = env->GetStaticFieldID(configClass, "RGB_565", "Landroid/graphics/Bitmap$Config;");
    jobject config = env->GetStaticObjectField(configClass, configField);

    jobject srcBitmap = env->CallStaticObjectMethod(bitmapClass, createBitmapMethod, decorViewWidth, decorViewHeight, config);

    jclass canvasClass = env->FindClass("android/graphics/Canvas");
    jmethodID canvasConstructor = env->GetMethodID(canvasClass, "<init>", "(Landroid/graphics/Bitmap;)V");
    jobject canvas = env->NewObject(canvasClass, canvasConstructor, srcBitmap);

    jmethodID drawMethod = env->GetMethodID(decorViewClass, "draw", "(Landroid/graphics/Canvas;)V");
    env->CallVoidMethod(decorView, drawMethod, canvas);

    return srcBitmap;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_natives_NativeImageProcessorImpl_initBlur(JNIEnv *env, jobject thiz, jint width,
                                                                      jint height, jint radius, jdouble resize_ratio) {
    BlurManager &blurManager = BlurManager::getInstance();
    blurManager.initBlur(env, thiz, width, height, radius, resize_ratio);
    blurManager.bitmapClass = env->FindClass("android/graphics/Bitmap");
}

extern "C"
JNIEXPORT jobject JNICALL
Java_io_github_pknujsp_blur_natives_NativeImageProcessorImpl_blur_Landroid_graphics_Bitmap_2IIID(JNIEnv *env, jobject thiz, jobject src_bitmap,
                                                                                                 jint width,
                                                                                                 jint height, jint radius,
                                                                                                 jdouble resize_ratio) {
    try {
        const SharedValues *sharedValues = init(width, height, radius, resize_ratio);

        if (sharedValues->isResized) {
            src_bitmap = resize(env, sharedValues->targetWidth, sharedValues->targetHeight, src_bitmap);
        }

        AndroidBitmapInfo info;
        void *pixels = nullptr;

        if ((AndroidBitmap_getInfo(env, src_bitmap, &info)) < 0) return nullptr;
        if ((AndroidBitmap_lockPixels(env, src_bitmap, (void **) &pixels)) < 0) return nullptr;
        blur((short *) pixels, sharedValues);

        AndroidBitmap_unlockPixels(env, src_bitmap);

        delete sharedValues;
        return src_bitmap;
    } catch (const char *e) {
        jthrowable throwable = env->ExceptionOccurred();
        return throwable;
    }
}


extern "C"
JNIEXPORT jobject JNICALL
Java_io_github_pknujsp_blur_natives_NativeImageProcessorImpl_blur_Landroid_graphics_Bitmap_2(JNIEnv *env, jobject thiz,
                                                                                             jobject src_bitmap) {
    BlurManager &blurManager = BlurManager::getInstance();
    if (blurManager.sharedValues->isResized) {
        src_bitmap = resize(env, blurManager.sharedValues->targetWidth, blurManager.sharedValues->targetHeight, src_bitmap);
    }
    return blurManager.startBlur(env, src_bitmap);
}
