LOCAL_PATH := $(call my-dir)

###########################
# Rust compilation outputs
###########################

include $(CLEAR_VARS)
LOCAL_MODULE := myfuck-rs
LOCAL_SRC_FILES := ../out/$(TARGET_ARCH_ABI)/libmyfuck-rs.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := boot-rs
LOCAL_SRC_FILES := ../out/$(TARGET_ARCH_ABI)/libmyfuckboot-rs.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := init-rs
LOCAL_SRC_FILES := ../out/$(TARGET_ARCH_ABI)/libmyfuckinit-rs.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := policy-rs
LOCAL_SRC_FILES := ../out/$(TARGET_ARCH_ABI)/libmyfuckpolicy-rs.a
include $(PREBUILT_STATIC_LIBRARY)
