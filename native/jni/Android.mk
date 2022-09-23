LOCAL_PATH := $(call my-dir)

########################
# Binaries
########################

# Global toggle for the WIP zygote injection features
ENABLE_INJECT := 0

ifdef B_MYFUCK

include $(CLEAR_VARS)
LOCAL_MODULE := myfuck
LOCAL_STATIC_LIBRARIES := libnanopb libsystemproperties libutils
LOCAL_C_INCLUDES := jni/include

LOCAL_SRC_FILES := \
    core/applets.cpp \
    core/myfuck.cpp \
    core/daemon.cpp \
    core/bootstages.cpp \
    core/socket.cpp \
    core/db.cpp \
    core/scripting.cpp \
    core/restorecon.cpp \
    core/module.cpp \
    myfuckhide/myfuckhide.cpp \
    myfuckhide/hide_utils.cpp \
    myfuckhide/hide_policy.cpp \
    resetprop/persist_properties.cpp \
    resetprop/resetprop.cpp \
    vigo/vigo.cpp \
    vigo/connect.cpp \
    vigo/pts.cpp \
    vigo/su_daemon.cpp

LOCAL_LDLIBS := -llog
LOCAL_CPPFLAGS := -DENABLE_INJECT=$(ENABLE_INJECT)

ifeq ($(ENABLE_INJECT),1)
LOCAL_STATIC_LIBRARIES += libxhook
LOCAL_SRC_FILES += \
    inject/entry.cpp \
    inject/utils.cpp \
    inject/hook.cpp
else
LOCAL_SRC_FILES += myfuckhide/proc_monitor.cpp
endif

include $(BUILD_EXECUTABLE)

endif

include $(CLEAR_VARS)

ifdef B_INIT

LOCAL_MODULE := myfuckinit
LOCAL_STATIC_LIBRARIES := libsepol libxz libutils
LOCAL_C_INCLUDES := jni/include out

LOCAL_SRC_FILES := \
    init/init.cpp \
    init/mount.cpp \
    init/rootdir.cpp \
    init/getinfo.cpp \
    init/twostage.cpp \
    init/raw_data.cpp \
    core/socket.cpp \
    myfuckpolicy/sepolicy.cpp \
    myfuckpolicy/myfuckpolicy.cpp \
    myfuckpolicy/rules.cpp \
    myfuckpolicy/policydb.cpp \
    myfuckpolicy/statement.cpp \
    myfuckboot/pattern.cpp

LOCAL_LDFLAGS := -static
include $(BUILD_EXECUTABLE)

endif

ifdef B_BOOT

include $(CLEAR_VARS)
LOCAL_MODULE := myfuckboot
LOCAL_STATIC_LIBRARIES := libmincrypt liblzma liblz4 libbz2 libfdt libutils libz
LOCAL_C_INCLUDES := jni/include

LOCAL_SRC_FILES := \
    myfuckboot/main.cpp \
    myfuckboot/bootimg.cpp \
    myfuckboot/hexpatch.cpp \
    myfuckboot/compress.cpp \
    myfuckboot/format.cpp \
    myfuckboot/dtb.cpp \
    myfuckboot/ramdisk.cpp \
    myfuckboot/pattern.cpp \
    utils/cpio.cpp

LOCAL_LDFLAGS := -static
include $(BUILD_EXECUTABLE)

endif

ifdef B_POLICY

include $(CLEAR_VARS)
LOCAL_MODULE := myfuckpolicy
LOCAL_STATIC_LIBRARIES := libsepol libutils
LOCAL_C_INCLUDES := jni/include

LOCAL_SRC_FILES := \
    core/applet_stub.cpp \
    myfuckpolicy/sepolicy.cpp \
    myfuckpolicy/myfuckpolicy.cpp \
    myfuckpolicy/rules.cpp \
    myfuckpolicy/policydb.cpp \
    myfuckpolicy/statement.cpp

LOCAL_CFLAGS := -DAPPLET_STUB_MAIN=myfuckpolicy_main
LOCAL_LDFLAGS := -static
include $(BUILD_EXECUTABLE)

endif

ifdef B_PROP

include $(CLEAR_VARS)
LOCAL_MODULE := resetprop
LOCAL_STATIC_LIBRARIES := libnanopb libsystemproperties libutils
LOCAL_C_INCLUDES := jni/include

LOCAL_SRC_FILES := \
    core/applet_stub.cpp \
    resetprop/persist_properties.cpp \
    resetprop/resetprop.cpp \

LOCAL_CFLAGS := -DAPPLET_STUB_MAIN=resetprop_main
LOCAL_LDFLAGS := -static
include $(BUILD_EXECUTABLE)

endif

ifdef B_TEST
ifneq (,$(wildcard jni/test.cpp))

include $(CLEAR_VARS)
LOCAL_MODULE := test
LOCAL_STATIC_LIBRARIES := libutils
LOCAL_C_INCLUDES := jni/include
LOCAL_SRC_FILES := test.cpp
include $(BUILD_EXECUTABLE)

endif
endif

ifdef B_BB

include jni/external/busybox/Android.mk

endif

########################
# Libraries
########################
include jni/utils/Android.mk
include jni/external/Android.mk
