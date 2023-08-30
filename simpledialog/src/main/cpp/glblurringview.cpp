//
// Created by jesp on 2023-07-04.
//

#include "glblurringview.h"
#include <vector>
#include <string>
#include <cstring>
#include <android/log.h>
#include <mutex>
#include <queue>

#define LOG_TAG "GLBlurringView"
#define ANDROID_LOG_DEBUG 3
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

static const char *vertexShaderCode = R"(
    uniform mat4 uMVPMatrix;
    attribute vec4 vPosition;
    attribute vec2 a_texCoord;
    varying vec2 v_texCoord;
    void main() {
      gl_Position = uMVPMatrix * vPosition;
      v_texCoord = a_texCoord;
    }
)";

static const char *fragmentShaderCode = R"(
    precision mediump float;
    varying vec2 v_texCoord;
    uniform sampler2D s_texture;
    void main() {
      gl_FragColor = texture2D(s_texture, v_texCoord);
    }
)";

static const unsigned int indices[] = {0, 1, 2, 2, 3, 0};

std::queue<std::pair<jobject, void *>> mQueue;
std::mutex mMutex;

jmethodID requestRenderMethodId = nullptr;
jobject glSurfaceView = nullptr;

GLuint verticesBufferObj;
GLuint uvsBufferObj;
GLuint indexBufferObj;

static GLint positionHandle = 0;
static GLint uvHandle = 0;
static GLuint program = 0;
static GLint mvpMatrixHandle = 0;

static GLuint textures;
static GLfloat vpMatrix[16];
static GLfloat modelMatrix[16];
static GLfloat mvpMatrix[16];

GLint bitmapWidth = 0;
GLint bitmapHeight = 0;

static GLuint loadShader(GLenum type, const char *shaderCode);

static void multiplyMM(GLfloat *result, int resultOffset, GLfloat *lhs, int lhsOffset, GLfloat *rhs, int rhsOffset);

static bool overlap(const GLfloat *a, int aStart, int aLength, const GLfloat *b, int bStart, int bLength);

static void setIdentityM(GLfloat sm[16], int smOffset);

static void initUvs(GLfloat statusBarHeight, GLfloat navigationBarHeight, GLfloat windowHeight);

static void initVerticies(GLfloat statusBarHeight, GLfloat navigationBarHeight, GLfloat windowHeight);

static void initIndicies();

static void printError(const std::string &msg);

static void printError(const char *msg) {
    auto error = glGetError();
    if (error != GL_NO_ERROR) {
        LOGD("%s Err: %d", msg, error);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_natives_NativeGLBlurringImpl_00024Companion_onSurfaceCreated(JNIEnv *env, jobject thiz, jobject blurring_view) {
    program = glCreateProgram();
    glAttachShader(program, loadShader(GL_VERTEX_SHADER, vertexShaderCode));
    glAttachShader(program, loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode));
    glLinkProgram(program);

    setIdentityM(modelMatrix, 0);
    setIdentityM(vpMatrix, 0);

    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

    glSurfaceView = env->NewGlobalRef(blurring_view);
    jclass glSurfaceViewClass = env->GetObjectClass(glSurfaceView);
    requestRenderMethodId = env->GetMethodID(glSurfaceViewClass, "requestRender", "()V");
}

static GLuint loadShader(GLenum type, const char *shaderCode) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &shaderCode, nullptr);
    glShaderSource(shader, 1, &shaderCode, nullptr);
    glCompileShader(shader);
    return shader;
}


extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_natives_NativeGLBlurringImpl_00024Companion_onDrawFrame(JNIEnv *env, jobject thiz, jobject bitmap) {
    void *pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels) != 0) return;

    glClear(GL_COLOR_BUFFER_BIT bitor GL_DEPTH_BUFFER_BIT);

    glBindTexture(GL_TEXTURE_2D, textures);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmapWidth, bitmapHeight, 0,
                 GL_RGBA, GL_UNSIGNED_BYTE, pixels);

    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, nullptr);

    AndroidBitmap_unlockPixels(env, bitmap);
}


static void multiplyMM(GLfloat *result, int resultOffset, GLfloat *lhs, int lhsOffset, GLfloat *rhs, int rhsOffset) {
    if (overlap(result, resultOffset, 16, lhs, lhsOffset, 16)
        || overlap(result, resultOffset, 16, rhs, rhsOffset, 16)) {
        float tmp[16];
        for (int i = 0; i < 4; i++) {
            float rhs_i0 = rhs[4 * i + 0 + rhsOffset];
            float ri0 = lhs[0 + lhsOffset] * rhs_i0;
            float ri1 = lhs[1 + lhsOffset] * rhs_i0;
            float ri2 = lhs[2 + lhsOffset] * rhs_i0;
            float ri3 = lhs[3 + lhsOffset] * rhs_i0;
            for (int j = 1; j < 4; j++) {
                float rhs_ij = rhs[4 * i + j + rhsOffset];
                ri0 += lhs[4 * j + 0 + lhsOffset] * rhs_ij;
                ri1 += lhs[4 * j + 1 + lhsOffset] * rhs_ij;
                ri2 += lhs[4 * j + 2 + lhsOffset] * rhs_ij;
                ri3 += lhs[4 * j + 3 + lhsOffset] * rhs_ij;
            }
            tmp[4 * i + 0] = ri0;
            tmp[4 * i + 1] = ri1;
            tmp[4 * i + 2] = ri2;
            tmp[4 * i + 3] = ri3;
        }

        for (int i = 0; i < 16; i++) {
            result[i + resultOffset] = tmp[i];
        }
    } else {
        for (int i = 0; i < 4; i++) {
            float rhs_i0 = rhs[4 * i + 0 + rhsOffset];
            float ri0 = lhs[0 + lhsOffset] * rhs_i0;
            float ri1 = lhs[1 + lhsOffset] * rhs_i0;
            float ri2 = lhs[2 + lhsOffset] * rhs_i0;
            float ri3 = lhs[3 + lhsOffset] * rhs_i0;
            for (int j = 1; j < 4; j++) {
                float rhs_ij = rhs[4 * i + j + rhsOffset];
                ri0 += lhs[4 * j + 0 + lhsOffset] * rhs_ij;
                ri1 += lhs[4 * j + 1 + lhsOffset] * rhs_ij;
                ri2 += lhs[4 * j + 2 + lhsOffset] * rhs_ij;
                ri3 += lhs[4 * j + 3 + lhsOffset] * rhs_ij;
            }
            result[4 * i + 0 + resultOffset] = ri0;
            result[4 * i + 1 + resultOffset] = ri1;
            result[4 * i + 2 + resultOffset] = ri2;
            result[4 * i + 3 + resultOffset] = ri3;
        }
    }
}

static bool overlap(const GLfloat *a, int aStart, int aLength, const GLfloat *b, int bStart, int bLength) {
    if (a != b)
        return false;
    if (aStart == bStart)
        return true;

    int aEnd = aStart + aLength;
    int bEnd = bStart + bLength;

    if (aEnd == bEnd)
        return true;
    if (aStart < bStart && bStart < aEnd)
        return true;
    if (aStart < bEnd && bEnd < aEnd)
        return true;
    if (bStart < aStart && aStart < bEnd)
        return true;
    if (bStart < aEnd && aEnd < bEnd)
        return true;

    return false;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_natives_NativeGLBlurringImpl_00024Companion_onSurfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height,
                                                                                         jintArray collecting_view_rect, jintArray window_rect) {
    glViewport(0, 0, width, height);

    jint *collecting_view_rect_array = env->GetIntArrayElements(collecting_view_rect, nullptr);
    jint *window_rect_array = env->GetIntArrayElements(window_rect, nullptr);

    const auto statusBarHeight = (GLfloat) collecting_view_rect_array[1];
    const auto navigationBarHeight = (GLfloat) (window_rect_array[3] - collecting_view_rect_array[3]);
    const auto windowHeight = (GLfloat) window_rect_array[3];

    bitmapWidth = width;
    bitmapHeight = (GLint) windowHeight;

    delete[] collecting_view_rect_array;
    delete[] window_rect_array;

    uvHandle = glGetAttribLocation(program, "a_texCoord");
    positionHandle = glGetAttribLocation(program, "vPosition");
    mvpMatrixHandle = glGetUniformLocation(program, "uMVPMatrix");

    //glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix);
    setIdentityM(mvpMatrix, 0);

    glUseProgram(program);

    initIndicies();
    initVerticies(statusBarHeight, navigationBarHeight, windowHeight);
    initUvs(statusBarHeight, navigationBarHeight, windowHeight);

    //multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0);
    glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix);

    glGenTextures(1, &textures);
    glActiveTexture(GL_TEXTURE0);

}

static void initIndicies() {
    glGenBuffers(1, &indexBufferObj);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObj);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), &indices[0], GL_STATIC_DRAW);
}

static void initVerticies(GLfloat statusBarHeight, GLfloat navigationBarHeight, GLfloat windowHeight) {
    const GLfloat vertices[] = {
            -1.0f, -1.0f, 0.0f,  // bottom left
            1.0f, -1.0f, 0.0f,  // bottom right
            1.0f, 1.0f, 0.0f,  // top right
            -1.0f, 1.0f, 0.0f,  // top left
    };

    glGenBuffers(1, &verticesBufferObj);
    glEnableVertexAttribArray(positionHandle);

    glBindBuffer(GL_ARRAY_BUFFER, verticesBufferObj);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), &vertices[0], GL_STATIC_DRAW);

    glVertexAttribPointer(positionHandle, 3, GL_FLOAT, false, 3 * sizeof(GLfloat), nullptr);
}

static void initUvs(GLfloat statusBarHeight, GLfloat navigationBarHeight, GLfloat windowHeight) {
    const GLfloat statusBarRatio = statusBarHeight / windowHeight;
    const GLfloat navigationBarRatio = navigationBarHeight / windowHeight;

    const GLfloat uvs[] = {
            0.0f, 1.0f - navigationBarRatio,
            1.0f, 1.0f - navigationBarRatio,
            1.0f, statusBarRatio,
            0.0f, statusBarRatio,
    };

    glGenBuffers(1, &uvsBufferObj);
    glEnableVertexAttribArray(uvHandle);

    glBindBuffer(GL_ARRAY_BUFFER, uvsBufferObj);
    glBufferData(GL_ARRAY_BUFFER, sizeof(uvs), &uvs[0], GL_STATIC_DRAW);

    glVertexAttribPointer(uvHandle, 2, GL_FLOAT, GL_FALSE, 0, nullptr);
}

static void setIdentityM(GLfloat *sm, int smOffset) {
    for (int i = 0; i < 16; i++) {
        sm[smOffset + i] = 0;
    }
    for (int i = 0; i < 16; i += 5) {
        sm[smOffset + i] = 1.0f;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_natives_NativeGLBlurringImpl_00024Companion_onPause(JNIEnv *env, jobject thiz) {
    glDeleteTextures(1, &textures);
    glDeleteBuffers(1, &verticesBufferObj);
    glDeleteBuffers(1, &uvsBufferObj);
    glDeleteBuffers(1, &indexBufferObj);
    glDeleteProgram(program);

    env->DeleteGlobalRef(glSurfaceView);
    requestRenderMethodId = nullptr;
}
