#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/window.h>
#include <GLES3/gl31.h>
#include <GLES3/gl3ext.h>
#include "blur.h"

#define TAG "NativeImageProcessor"
#define ANDROID_LOG_DEBUG 3
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)


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
jobject resize(JNIEnv *env, jint newWidth, jint newHeight, _jobject *bitmap, jclass bitmapClass) {
    jmethodID createScaledBitmapMethod = env->GetStaticMethodID(bitmapClass, "createScaledBitmap",
                                                                "(Landroid/graphics/Bitmap;IIZ)Landroid/graphics/Bitmap;");
    jobject resizedBitmap = env->CallStaticObjectMethod(bitmapClass, createScaledBitmapMethod, bitmap, newWidth,
                                                        newHeight, true);
    return resizedBitmap;
}

extern "C"
jobject toBitmap(JNIEnv *env, jobject decorView, _jclass *decorViewClass, _jclass *bitmapClass, jint contentWidth, jint contentHeight,
                 jint statusBarHeight, jint navigationBarHeight) {
    jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                          "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    jclass configClass = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID configField = env->GetStaticFieldID(configClass, "RGB_565", "Landroid/graphics/Bitmap$Config;");
    jobject config = env->GetStaticObjectField(configClass, configField);

    jobject srcBitmap = env->CallStaticObjectMethod(bitmapClass, createBitmapMethod, contentWidth, contentHeight, config);

    jclass canvasClass = env->FindClass("android/graphics/Canvas");
    jmethodID canvasConstructor = env->GetMethodID(canvasClass, "<init>", "(Landroid/graphics/Bitmap;)V");
    jobject canvas = env->NewObject(canvasClass, canvasConstructor, srcBitmap);

    jmethodID translateMethod = env->GetMethodID(canvasClass, "translate", "(FF)V");
    env->CallVoidMethod(canvas, translateMethod, 0.0f, (jfloat) -statusBarHeight);

    jmethodID drawMethod = env->GetMethodID(decorViewClass, "draw", "(Landroid/graphics/Canvas;)V");
    env->CallVoidMethod(decorView, drawMethod, canvas);

    return srcBitmap;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_io_github_pknujsp_blur_NativeImageProcessor_applyBlur(JNIEnv *env, jobject thiz, jobject decorView, jint radius, jdouble resizeRatio,
                                                           jint statusBarHeight, jint navigationBarHeight) {
    try {
        jclass decorViewClass = env->GetObjectClass(decorView);

        jmethodID getWidthMethod = env->GetMethodID(decorViewClass, "getWidth", "()I");
        jmethodID getHeightMethod = env->GetMethodID(decorViewClass, "getHeight", "()I");

        if (getWidthMethod == nullptr || getHeightMethod == nullptr)
            return nullptr;

        const jint contentWidth = env->CallIntMethod(decorView, getWidthMethod);
        const jint contentHeight = env->CallIntMethod(decorView, getHeightMethod) - navigationBarHeight - statusBarHeight;

        jclass bitmapClass = env->FindClass("android/graphics/Bitmap");

        jint newWidth = (jint) (contentWidth / resizeRatio);
        jint newHeight = (jint) (contentHeight / resizeRatio);
        if (newWidth % 2 != 0) newWidth--;
        if (newHeight % 2 != 0) newHeight--;

        jobject srcBitmap = toBitmap(env, decorView, decorViewClass, bitmapClass, contentWidth, contentHeight, statusBarHeight,
                                     navigationBarHeight);

        srcBitmap = resize(env, newWidth, newHeight, srcBitmap, bitmapClass);

        AndroidBitmapInfo info;
        void *pixels = nullptr;

        if ((AndroidBitmap_getInfo(env, srcBitmap, &info)) < 0) return nullptr;
        if ((AndroidBitmap_lockPixels(env, srcBitmap, (void **) &pixels)) < 0) return nullptr;

        const auto srcSize = (jsize) (newWidth * newHeight);
        jshortArray result = env->NewShortArray(srcSize);
        env->SetShortArrayRegion(result, 0, srcSize, (jshort *) pixels);

        blur((u_short *) pixels, radius, newWidth, newHeight);

        AndroidBitmap_unlockPixels(env, srcBitmap);
        return srcBitmap;
    } catch (const char *e) {
        jthrowable throwable = env->ExceptionOccurred();
        return throwable;
    }
}
