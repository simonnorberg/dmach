# Copyright (C) 2014 Simon Norberg
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

LOCAL_PATH := $(call my-dir)

PD_C_INCLUDES := $(LOCAL_PATH)/../../../../PdCore/jni/libpd/pure-data/src

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
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../../../PdCore/libs/$(TARGET_ARCH_ABI) -lpd
LOCAL_STATIC_LIBRARIES := sic
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------
