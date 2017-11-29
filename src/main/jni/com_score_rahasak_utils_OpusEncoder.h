#include <jni.h>

#ifndef _Included_com_score_rahasak_utils_OpusEncoder
#define _Included_com_score_rahasak_utils_OpusEncoder
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_score_rahasak_utils_OpusEncoder_nativeInitEncoder
  (JNIEnv *, jobject, jint, jint, jint);

JNIEXPORT jint JNICALL
Java_com_score_rahasak_utils_OpusEncoder_nativeSetBitrate(JNIEnv *env, jobject instance,
                                                          jint bitrate);

JNIEXPORT jint JNICALL Java_com_score_rahasak_utils_OpusEncoder_nativeEncodeShorts
        (JNIEnv *, jobject, jshortArray, jint, jbyteArray);

JNIEXPORT jint JNICALL Java_com_score_rahasak_utils_OpusEncoder_nativeEncodeBytes
        (JNIEnv *, jobject, jbyteArray, jint, jbyteArray);

JNIEXPORT jboolean JNICALL Java_com_score_rahasak_utils_OpusEncoder_nativeReleaseEncoder
  (JNIEnv *, jobject);

JNIEXPORT jint JNICALL
Java_com_score_rahasak_utils_OpusEncoder_nativeSetComplexity(JNIEnv *env, jobject instance,
                                                             jint complexity);

#ifdef __cplusplus
}
#endif
#endif
