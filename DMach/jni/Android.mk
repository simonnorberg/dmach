LOCAL_PATH := $(call my-dir)

PD_C_INCLUDES := $(LOCAL_PATH)/../../../../pd-for-android/PdCore/jni/libpd/pure-data/src

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := shared
LOCAL_C_INCLUDES := $(PD_C_INCLUDES)
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := shared.c
include $(BUILD_STATIC_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := loud
LOCAL_C_INCLUDES := $(PD_C_INCLUDES)
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := loud.c
include $(BUILD_STATIC_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := sic
LOCAL_C_INCLUDES := $(PD_C_INCLUDES)
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := sic.c
LOCAL_STATIC_LIBRARIES := shared loud
include $(BUILD_STATIC_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := tanh_tilde
LOCAL_C_INCLUDES := $(PD_C_INCLUDES)
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := tanh.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../../../pd-for-android/PdCore/libs/$(TARGET_ARCH_ABI) -lpd
LOCAL_STATIC_LIBRARIES := sic
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------