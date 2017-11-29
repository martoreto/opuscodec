LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -llog

LOCAL_MODULE:= senz

#################### COMPILE OPTIONS #######################

# Uncomment this for fixed-point build
#FIXED_POINT=1

# It is strongly recommended to uncomment one of these
#	VAR_ARRAYS: Use C99 variable-length arrays for stack allocation
# 	USE_ALLOCA: Use alloca() for stack allocation
# 	If none is defined, then the fallback is a non-threadsafe global array
LOCAL_CFLAGS := -DUSE_ALLOCA
#CFLAGS := -DVAR_ARRAYS $(CFLAGS)

# These options affect performance
#	HAVE_LRINTF: Use C99 intrinsics to speed up float-to-int conversion
#   inline: Don't use the 'inline' keyword (for ANSI C compilers)
#   restrict: Don't use the 'restrict' keyword (for pre-C99 compilers)
LOCAL_CFLAGS += -DHAVE_LRINTF
#CFLAGS := -Dinline= $(CFLAGS)
#LOCAL_CFLAGS := -Drestrict= $(CFLAGS)

OPUS_VERSION := "1.0.3"
PACKAGE_VERSION := $(OPUS_VERSION)
LOCAL_CFLAGS += -DOPUS_VERSION='$(OPUS_VERSION)'
WARNINGS := -Wall -W -Wstrict-prototypes -Wextra -Wcast-align -Wnested-externs -Wshadow
LOCAL_CFLAGS += -O2 -g $(WARNINGS) -DOPUS_BUILD

# TODO: add libNE10 for ARM

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
LOCAL_ARM_NEON := true
LOCAL_CFLAGS += -DOPUS_ARM_ASM -DOPUS_ARM_INLINE_EDSP -DOPUS_ARM_INLINE_MEDIA \
				-DOPUS_ARM_INLINE_ASM -DOPUS_ARM_INLINE_EDSP -DOPUS_ARM_INLINE_NEON \
				-DOPUS_ARM_MAY_HAVE_EDSP -DOPUS_ARM_MAY_HAVE_MEDIA -DOPUS_ARM_MAY_HAVE_NEON \
				-DOPUS_ARM_PRESUME_EDSP -DOPUS_ARM_PRESUME_MEDIA \
				-DOPUS_ARM_MAY_HAVE_NEON_INTR -DOPUS_HAVE_RTCD
CPU_ARM=1
HAVE_ARM_NEON_INTR=1
endif

ifeq ($(TARGET_ARCH_ABI),x86)
LOCAL_CFLAGS += -msse4.2 -mavx
LOCAL_CFLAGS += -DOPUS_X86_MAY_HAVE_SSE -DOPUS_X86_MAY_HAVE_SSE2 -DOPUS_X86_MAY_HAVE_SSE4_1 \
				-DOPUS_X86_MAY_HAVE_AVX -DOPUS_X86_PRESUME_SSE -DOPUS_X86_PRESUME_SSE2 \
				-DOPUS_HAVE_RTCD -DCPU_INFO_BY_C
HAVE_SSE=1
HAVE_SSE2=1
HAVE_SSE4_1=1
endif

ifeq ($(TARGET_ARCH_ABI),x86_64)
LOCAL_CFLAGS += -mavx
LOCAL_CFLAGS += -DOPUS_X86_MAY_HAVE_SSE -DOPUS_X86_MAY_HAVE_SSE2 -DOPUS_X86_MAY_HAVE_SSE4_1 \
				-DOPUS_X86_MAY_HAVE_AVX -DOPUS_X86_PRESUME_SSE -DOPUS_X86_PRESUME_SSE2 \
				-DOPUS_X86_PRESUME_SSE4_1 -DOPUS_HAVE_RTCD -DCPU_INFO_BY_C
HAVE_SSE=1
HAVE_SSE2=1
HAVE_SSE4_1=1
endif

include $(LOCAL_PATH)/opus/silk_sources.mk
include $(LOCAL_PATH)/opus/celt_sources.mk
include $(LOCAL_PATH)/opus/opus_sources.mk

ifdef FIXED_POINT
SILK_SOURCES += $(SILK_SOURCES_FIXED)
ifdef HAVE_SSE4_1
SILK_SOURCES += $(SILK_SOURCES_SSE4_1) $(SILK_SOURCES_FIXED_SSE4_1)
endif
ifdef HAVE_ARM_NEON_INTR
SILK_SOURCES += $(SILK_SOURCES_FIXED_ARM_NEON_INTR)
endif
else
SILK_SOURCES += $(SILK_SOURCES_FLOAT)
ifdef HAVE_SSE4_1
SILK_SOURCES += $(SILK_SOURCES_SSE4_1)
endif
endif

ifdef FIXED_POINT
else
OPUS_SOURCES += $(OPUS_SOURCES_FLOAT)
endif

ifdef HAVE_SSE
CELT_SOURCES += $(CELT_SOURCES_SSE)
endif
ifdef HAVE_SSE2
CELT_SOURCES += $(CELT_SOURCES_SSE2)
endif
ifdef HAVE_SSE4_1
CELT_SOURCES += $(CELT_SOURCES_SSE4_1)
endif

ifdef CPU_ARM
CELT_SOURCES += $(CELT_SOURCES_ARM)
SILK_SOURCES += $(SILK_SOURCES_ARM)

ifdef HAVE_ARM_NEON_INTR
CELT_SOURCES += $(CELT_SOURCES_ARM_NEON_INTR)
SILK_SOURCES += $(SILK_SOURCES_ARM_NEON_INTR)
endif

ifdef HAVE_ARM_NE10
CELT_SOURCES += $(CELT_SOURCES_ARM_NE10)
endif
endif

LOCAL_SRC_FILES := \
	$(patsubst %,$(LOCAL_PATH)/opus/%,$(SILK_SOURCES) $(CELT_SOURCES) $(OPUS_SOURCES)) \
    com_score_rahasak_utils_OpusEncoder.c \
    com_score_rahasak_utils_OpusDecoder.c

all:
	echo $(LOCAL_SRC_FILES)

LOCAL_C_INCLUDES += $(LOCAL_PATH)/opus/include/ \
	$(LOCAL_PATH)/opus/silk/ \
	$(LOCAL_PATH)/opus/silk/fixed \
	$(LOCAL_PATH)/opus/silk/float \
	$(LOCAL_PATH)/opus/celt/ \
	$(LOCAL_PATH)/opus/src/ \
	$(LOCAL_PATH)/opus/

ifdef FIXED_POINT
CFLAGS += -DFIXED_POINT=1 -DDISABLE_FLOAT_API
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/opus/silk/fixed
else
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/opus/silk/float
endif

LOCAL_STATIC_LIBRARIES := cpufeatures

# TARGET_ARCH_ABI := armeabi-v7a arm64-v8a

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)
