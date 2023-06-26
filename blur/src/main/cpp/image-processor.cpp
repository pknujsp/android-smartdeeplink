#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include "blur.h"

extern "C"
JNIEXPORT jboolean JNICALL
Java_io_github_pknujsp_blur_NativeImageProcessor_blur(JNIEnv *env, jobject thiz, jobject srcBitmap, jint radius, jint target_width,
                                                      jint target_height) {
    AndroidBitmapInfo info;
    void *pixels;
    int ret;

    if ((ret = AndroidBitmap_getInfo(env, srcBitmap, &info)) < 0) {
        return false;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return false;
    }

    if ((ret = AndroidBitmap_lockPixels(env, srcBitmap, &pixels)) < 0) {
        return false;
    }

    const auto srcSize = (jsize) (info.width * info.height);
    jintArray result = env->NewIntArray(srcSize);
    env->SetIntArrayRegion(result, 0, srcSize, (jint *) pixels);

    blur((unsigned int *) pixels, radius, target_width, target_height);

    AndroidBitmap_unlockPixels(env, srcBitmap);

    return true;
}
