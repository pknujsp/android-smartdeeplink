cmake_minimum_required(VERSION 3.22.1)

project("RenderScript Toolkit")

set(can_use_assembler TRUE)
enable_language(ASM)
add_definitions(-v -DANDROID -DOC_ARM_ASM)
set(CMAKE_CXX_FLAGS "-Wall -Wextra ${CMAKE_CXX_FLAGS} -std=c++17")
add_definitions(-DVK_USE_PLATFORM_ANDROID_KHR=1)

if (CMAKE_SYSTEM_PROCESSOR STREQUAL armv7-a)
  add_definitions(-DARCH_ARM_USE_INTRINSICS -DARCH_ARM_HAVE_VFP)
  set(ASM_SOURCES
          toolkit/Blend_neon.S
          toolkit/Blur_neon.S
          toolkit/ColorMatrix_neon.S
          toolkit/Convolve_neon.S
          toolkit/Lut3d_neon.S
          toolkit/Resize_neon.S
          toolkit/YuvToRgb_neon.S)
endif ()

if (CMAKE_SYSTEM_PROCESSOR STREQUAL aarch64)
  add_definitions(-DARCH_ARM_USE_INTRINSICS -DARCH_ARM64_USE_INTRINSICS -DARCH_ARM64_HAVE_NEON)
  set(ASM_SOURCES
          toolkit/Blend_advsimd.S
          toolkit/Blur_advsimd.S
          toolkit/ColorMatrix_advsimd.S
          toolkit/Convolve_advsimd.S
          toolkit/Lut3d_advsimd.S
          toolkit/Resize_advsimd.S
          toolkit/YuvToRgb_advsimd.S)
endif ()

add_library(# Sets the name of the library.
        renderscript-toolkit
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        toolkit/Blend.cpp
        toolkit/Blur.cpp
        toolkit/ColorMatrix.cpp
        toolkit/Convolve3x3.cpp
        toolkit/Convolve5x5.cpp
        toolkit/Histogram.cpp
        toolkit/JniEntryPoints.cpp
        toolkit/Lut.cpp
        toolkit/Lut3d.cpp
        toolkit/RenderScriptToolkit.cpp
        toolkit/Resize.cpp
        toolkit/TaskProcessor.cpp
        toolkit/Utils.cpp
        toolkit/YuvToRgb.cpp
        ${ASM_SOURCES})

add_library(
        stack-blur

        SHARED

        stackblur/blur.h
        stackblur/ABGR-StackBlur.cpp
        stackblur/abgr-stackblur.h
        stackblur/shared-values.h
        stackblur/threadpool.cpp
        stackblur/threadpool.h
        stackblur/RGB-StackBlur.cpp
        BlurManager.cpp
        BlurManager.h
)


add_library(
        gl-blurring

        SHARED

        glblurringview.cpp
        glblurringview.h
        stackblur/shared-values.h
)

find_library(
        log-lib
        log
)

find_library(
        jnigraphics-lib
        jnigraphics
)

find_library(EGL-lib EGL)
find_library(GLESv3-lib GLESv3)
find_library(ANDROID_LIB android)

target_link_libraries( # Specifies the target library.
        stack-blur

        ${log-lib}
        ${jnigraphics-lib}
        ${EGL-lib}
        ${GLESv3-lib}
        ${ANDROID_LIB}
        )


target_link_libraries( # Specifies the target library.
        gl-blurring

        vulkan
        stack-blur
        ${log-lib}
        ${jnigraphics-lib}
        ${EGL-lib}
        ${GLESv3-lib}
        ${ANDROID_LIB}
        )

target_link_libraries(# Specifies the target library.
        renderscript-toolkit

        cpufeatures
        jnigraphics
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

include(AndroidNdkModules)
android_ndk_import_module_cpufeatures()
