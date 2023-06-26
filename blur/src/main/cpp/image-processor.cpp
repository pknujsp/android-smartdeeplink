#include <jni.h>
#include <string>
#include <android/bitmap.h>

extern "C"
JNIEXPORT jintArray JNICALL
Java_io_github_pknujsp_blur_NativeImageProcessor_blur(JNIEnv *env, jobject thiz, jobject srcBitmap, jint radius, jint target_width,
                                                      jint target_height) {
    AndroidBitmapInfo info;
    void *pixels;
    int ret;

    if ((ret = AndroidBitmap_getInfo(env, srcBitmap, &info)) < 0) {
        return nullptr;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return nullptr;
    }

    if ((ret = AndroidBitmap_lockPixels(env, srcBitmap, &pixels)) < 0) {
        return nullptr;
    }

    const auto srcSize = (jsize) (info.width * info.height);
    jintArray result = env->NewIntArray(srcSize);
    env->SetIntArrayRegion(result, 0, info.height * info.width, (jint *) pixels);

    AndroidBitmap_unlockPixels(env, srcBitmap);

    return result;
}
