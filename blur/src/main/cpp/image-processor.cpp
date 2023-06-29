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
JNIEXPORT jobject JNICALL
Java_io_github_pknujsp_blur_NativeImageProcessor_applyBlur(JNIEnv *env, jobject thiz, jobject bitmap, jint width, jint height, jint radius,
                                                           jdouble resizeRatio, jint statusBarHeight, jint navigationBarHeight) {
    try {
        jclass windowClass = env->GetObjectClass(window);

        jmethodID getDecorViewMethod = env->GetMethodID(windowClass, "getDecorView", "()Landroid/view/View;");

        jobject decorView = env->CallObjectMethod(window, getDecorViewMethod);
        jclass decorViewClass = env->GetObjectClass(decorView);

        jmethodID findViewByIdMethod = env->GetMethodID(decorViewClass, "findViewById", "(I)Landroid/view/View;");

        // android.R.id.content
        jobject contentView = env->CallObjectMethod(decorView, findViewByIdMethod, 16908290);
        jclass contentViewClass = env->GetObjectClass(contentView);

        jmethodID getLocationInWindowMethod = env->GetMethodID(contentViewClass, "getLocationInWindow", "([I)V");

        jintArray locationOfContentViewInWindow = env->NewIntArray(2);
        env->CallVoidMethod(contentView, getLocationInWindowMethod, locationOfContentViewInWindow);

        jmethodID getWidthMethod = env->GetMethodID(contentViewClass, "getWidth", "()I");
        jmethodID getHeightMethod = env->GetMethodID(contentViewClass, "getHeight", "()I");

        const jint contentViewWidth = env->CallIntMethod(contentView, getWidthMethod);
        const jint contentViewHeight = env->CallIntMethod(contentView, getHeightMethod);

        jint *loc = env->GetIntArrayElements(locationOfContentViewInWindow, nullptr);

        const int rect[] = {
                loc[0],
                loc[1],
                loc[0] + contentViewWidth,
                loc[1] + contentViewHeight};

        jclass rectClass = env->FindClass("android/graphics/Rect");
        jmethodID rectConstructor = env->GetMethodID(rectClass, "<init>", "(IIII)V");

        jobject rectObject = env->NewObject(rectClass, rectConstructor, rect[0], rect[1], rect[2], rect[3]);

        jclass pixelCopyClass = env->FindClass("android/view/PixelCopy");
        jmethodID requestMethod = env->GetStaticMethodID(pixelCopyClass, "request",
                                                         "(Landroid/view/Window;Landroid/graphics/Rect;Landroid/graphics/Bitmap;Landroid/view/PixelCopy$OnPixelCopyFinishedListener;Landroid/os/Handler;)V");

        jclass handlerClass = env->FindClass("android/os/Handler");
        jmethodID handlerConstructor = env->GetMethodID(handlerClass, "<init>", "(Landroid/os/Looper;)V");
        jclass looperClass = env->FindClass("android/os/Looper");
        jmethodID getMainLooperMethod = env->GetStaticMethodID(looperClass, "getMainLooper", "()Landroid/os/Looper;");
        jobject looper = env->CallStaticObjectMethod(looperClass, getMainLooperMethod);

        jobject handler = env->NewObject(handlerClass, handlerConstructor, looper);

        jclass pixelCopyFinishedListenerInterface = env->FindClass("android/view/PixelCopy$OnPixelCopyFinishedListener");
        jmethodID onPixelCopyFinishedMethod = env->GetMethodID(pixelCopyFinishedListenerInterface, "onPixelCopyFinished",
                                                               "(I)V");


        jobject pixelCopyFinishedListener = env->NewObject(pixelCopyFinishedListenerInterface,);

        jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
        jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                              "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

        jclass configClass = env->FindClass("android/graphics/Bitmap$Config");
        jfieldID configField = env->GetStaticFieldID(configClass, "RGB_565", "Landroid/graphics/Bitmap$Config;");
        jobject config = env->GetStaticObjectField(configClass, configField);

        jobject srcBitmap = env->CallStaticObjectMethod(bitmapClass, createBitmapMethod, contentViewWidth, contentViewHeight, config);

        env->CallStaticVoidMethod(pixelCopyClass, requestMethod, window, rectObject, srcBitmap, onPixelCopyFinishedMethod, handler);

        jint newWidth = (jint) (contentViewHeight);
        jint newHeight = (jint) (contentViewWidth);
        if (newWidth % 2 != 0) newWidth--;
        if (newHeight % 2 != 0) newHeight--;

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
