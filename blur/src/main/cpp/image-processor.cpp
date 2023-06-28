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

jobject resizeBitmap(JNIEnv *env, jobject originalBitmap, jint width, jint height, jdouble resizeRatio) {
    /**
     val reducedSize = Size(
            (width / resizeRatio).toInt().let { if (it % 2 == 0) it else it - 1 },
    (height / resizeRatio).toInt().let { if (it % 2 == 0) it else it - 1 },
    )
    val pixels = WeakReference(IntArray(reducedSize.width * reducedSize.height)).get()!!
            val reducedBitmap = WeakReference(Bitmap.createScaledBitmap(originalBitmap, reducedSize.width, reducedSize.height, true)).get()!!
            reducedBitmap.getPixels(pixels, 0, reducedSize.width, 0, 0, reducedSize.width, reducedSize.height)
    originalBitmap.recycle()
     */

    jint newWidth = (jint) (width / resizeRatio);
    jint newHeight = (jint) (height / resizeRatio);

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createScaledBitmapMethod = env->GetStaticMethodID(bitmapClass, "createScaledBitmap",
                                                                "(Landroid/graphics/Bitmap;IIZ)Landroid/graphics/Bitmap;");

    jobject resizedBitmap = env->CallStaticObjectMethod(bitmapClass, createScaledBitmapMethod, originalBitmap, newWidth,
                                                        newHeight, true);

    return resizedBitmap;
}

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

/*
 * val width = decorView.width
      val height = decorView.height

      val locationRect = Rect(
        0,
        _statusBarHeight,
        width,
        height - _navigationBarHeight,
      )

      val originalBitmap = Bitmap.createBitmap(locationRect.width(), locationRect.height(), Bitmap.Config.RGB_565).applyCanvas {
        translate(0f, -_statusBarHeight.toFloat())
        decorView.draw(this)
      }
   val c = Canvas(this)
    c.block()
 */

jobject toBitmap(JNIEnv *env, jobject decor_view, jdouble resizeRatio, jint statusBarHeight, jint navigationBarHeight) {
    jclass viewClass = env->GetObjectClass(decor_view);
    jmethodID getWidthMethod = env->GetMethodID(viewClass, "getWidth", "()I");
    jmethodID getHeightMethod = env->GetMethodID(viewClass, "getHeight", "()I");

    if (getWidthMethod == nullptr || getHeightMethod == nullptr)
        return nullptr;

    const jint contentWidth = env->CallIntMethod(decor_view, getWidthMethod);
    const jint contentHeight = env->CallIntMethod(decor_view, getHeightMethod) - navigationBarHeight - statusBarHeight;

    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                          "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    jclass configClass = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID configField = env->GetStaticFieldID(configClass, "RGB_565", "Landroid/graphics/Bitmap$Config;");
    jobject config = env->GetStaticObjectField(configClass, configField);

    jobject bitmap = env->CallStaticObjectMethod(bitmapClass, createBitmapMethod, contentWidth, contentHeight, config);

    jclass canvasClass = env->FindClass("android/graphics/Canvas");
    jmethodID canvasConstructor = env->GetMethodID(canvasClass, "<init>", "(Landroid/graphics/Bitmap;)V");
    jobject canvas = env->NewObject(canvasClass, canvasConstructor, bitmap);

    // translate
    jmethodID translateMethod = env->GetMethodID(canvasClass, "translate", "(FF)V");
    env->CallVoidMethod(canvas, translateMethod, 0.0f, (jfloat) -statusBarHeight);

    jmethodID drawMethod = env->GetMethodID(viewClass, "draw", "(Landroid/graphics/Canvas;)V");
    env->CallVoidMethod(decor_view, drawMethod, canvas);

    if (resizeRatio >= 1.0)
        bitmap = resizeBitmap(env, bitmap, contentWidth, contentHeight, resizeRatio);

    return bitmap;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_io_github_pknujsp_blur_NativeImageProcessor_applyBlur(JNIEnv *env, jobject thiz, jobject decor_view, jint radius, jdouble resizeRatio,
                                                           jint statusBarHeight, jint navigationBarHeight, jint dimFactor) {
    jobject srcBitmap = toBitmap(env, decor_view, resizeRatio, statusBarHeight, navigationBarHeight);

    return srcBitmap;

    AndroidBitmapInfo info;

    if ((AndroidBitmap_getInfo(env, srcBitmap, &info)) < 0) return nullptr;
    if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) return nullptr;

    void *pixels;
    if ((AndroidBitmap_lockPixels(env, srcBitmap, &pixels)) < 0) return nullptr;

    const auto srcSize = (jsize) (info.width * info.height);
    jshortArray result = env->NewShortArray(srcSize);
    env->SetShortArrayRegion(result, 0, srcSize, (jshort *) pixels);

    //gl((unsigned int *) pixels, target_width, target_height);

    //dim((unsigned short *) pixels, (int) info.width, (int) info.height, dimFactor);
    blur((unsigned short *) pixels, radius % 2 == 0 ? radius + 1 : radius, (int) info.width, (int) info.height);

    AndroidBitmap_unlockPixels(env, srcBitmap);

    return srcBitmap;
}
