APP_ABI := armeabi-v7a x86
APP_CFLAGS := -Wall -Oz -fomit-frame-pointer -flto \
-D__MVSTR=${MYFUCK_VERSION} -D__MCODE=${MYFUCK_VER_CODE}
APP_LDFLAGS := -flto
APP_CPPFLAGS := -std=c++17
APP_STL := none
APP_PLATFORM := android-16

ifdef MYFUCK_DEBUG
APP_CFLAGS += -D__MDBG
endif

# Build 64 bit binaries
ifdef B_64BIT
APP_ABI += arm64-v8a x86_64
endif

# Busybox should use stock libc.a
ifdef B_BB
APP_PLATFORM := android-22
endif
