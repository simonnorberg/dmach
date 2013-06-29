LOCAL_PATH := $(call my-dir)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := shared
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../../../pd-for-android/PdCore/jni/libpd/pure-data/src
LOCAL_CFLAGS := -DPD
LOCAL_LDFLAGS := -WI,--export-dynamic
LOCAL_SRC_FILES := shared.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../../../pd-for-android/PdCore/libs/$(TARGET_ARCH_ABI) -lpd
include $(BUILD_STATIC_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := loud
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../../../pd-for-android/PdCore/jni/libpd/pure-data/src
LOCAL_CFLAGS := -DPD
LOCAL_LDFLAGS := -WI,--export-dynamic
LOCAL_SRC_FILES := loud.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../../../pd-for-android/PdCore/libs/$(TARGET_ARCH_ABI) -lpd
include $(BUILD_STATIC_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := sic
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../../../pd-for-android/PdCore/jni/libpd/pure-data/src
LOCAL_CFLAGS := -DPD
LOCAL_LDFLAGS := -WI,--export-dynamic
LOCAL_SRC_FILES := sic.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../../../pd-for-android/PdCore/libs/$(TARGET_ARCH_ABI) -lpd
LOCAL_STATIC_LIBRARIES := shared loud include
include $(BUILD_STATIC_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := tanh_tilde
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../../../pd-for-android/PdCore/jni/libpd/pure-data/src
LOCAL_CFLAGS := -DPD
LOCAL_LDFLAGS := -WI,--export-dynamic
LOCAL_SRC_FILES := tanh.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../../../pd-for-android/PdCore/libs/$(TARGET_ARCH_ABI) -lpd
LOCAL_STATIC_LIBRARIES := sic include
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------
