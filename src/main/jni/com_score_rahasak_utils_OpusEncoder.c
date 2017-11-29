#include <com_score_rahasak_utils_OpusEncoder.h>
#include "opus/include/opus.h"
#include <malloc.h>

JNIEXPORT jint JNICALL Java_com_score_rahasak_utils_OpusEncoder_nativeInitEncoder (JNIEnv *env, jobject obj, jint samplingRate, jint numberOfChannels, jint application)
{
	int error;
	int size;

	size = opus_encoder_get_size(1);
	OpusEncoder* enc = malloc(size);
	error = opus_encoder_init(enc, samplingRate, numberOfChannels, application);

	if (error) {
		free(enc);
	} else {
		jclass cls = (*env)->GetObjectClass(env, obj);
		jfieldID fid = (*env)->GetFieldID(env, cls, "address", "J");
		(*env)->SetLongField(env, obj, fid, (jlong)enc);
	}

	return error;
}

JNIEXPORT jint JNICALL Java_com_score_rahasak_utils_OpusEncoder_nativeSetBitrate(JNIEnv *env, jobject obj, jint bitrate) {
    jclass cls = (*env)->GetObjectClass(env, obj);
    jfieldID fid = (*env)->GetFieldID(env, cls, "address", "J");
    OpusEncoder* enc = (OpusEncoder*)((*env)->GetLongField(env, obj, fid));
    return opus_encoder_ctl(enc, OPUS_SET_BITRATE(bitrate));
}

JNIEXPORT jint JNICALL Java_com_score_rahasak_utils_OpusEncoder_nativeSetComplexity(JNIEnv *env, jobject obj, jint complexity) {
    jclass cls = (*env)->GetObjectClass(env, obj);
    jfieldID fid = (*env)->GetFieldID(env, cls, "address", "J");
    OpusEncoder* enc = (OpusEncoder*)((*env)->GetLongField(env, obj, fid));
    return opus_encoder_ctl(enc, OPUS_SET_COMPLEXITY(complexity));
}

JNIEXPORT jint JNICALL Java_com_score_rahasak_utils_OpusEncoder_nativeEncodeShorts (JNIEnv *env, jobject obj, jshortArray in, jint frames, jbyteArray out)
{
	jclass cls = (*env)->GetObjectClass(env, obj);
	jfieldID fid = (*env)->GetFieldID(env, cls, "address", "J");
	OpusEncoder* enc = (OpusEncoder*)((*env)->GetLongField(env, obj, fid));

	jint outputArraySize = (*env)->GetArrayLength(env, out);

	jshort* audioSignal = (*env)->GetShortArrayElements(env, in, 0);
	jbyte* encodedSignal = (*env)->GetByteArrayElements(env, out, 0);

	int dataArraySize = opus_encode(enc, audioSignal, frames,
									(unsigned char *) encodedSignal, outputArraySize);

	(*env)->ReleaseShortArrayElements(env,in,audioSignal,JNI_ABORT);
	(*env)->ReleaseByteArrayElements(env,out,encodedSignal,0);

	return dataArraySize;
}

JNIEXPORT jint JNICALL Java_com_score_rahasak_utils_OpusEncoder_nativeEncodeBytes (JNIEnv *env, jobject obj, jbyteArray in, jint frames, jbyteArray out)
{
	jclass cls = (*env)->GetObjectClass(env, obj);
	jfieldID fid = (*env)->GetFieldID(env, cls, "address", "J");
	OpusEncoder* enc = (OpusEncoder*)((*env)->GetLongField(env, obj, fid));

	jint outputArraySize = (*env)->GetArrayLength(env, out);

	jbyte* audioSignal = (*env)->GetByteArrayElements(env, in, 0);
	jbyte* encodedSignal = (*env)->GetByteArrayElements(env, out, 0);

    if (((unsigned long)audioSignal) % 2) {
        // Unaligned...
        return OPUS_BAD_ARG;
    }

	int dataArraySize = opus_encode(enc, (const opus_int16 *) audioSignal, frames,
									(unsigned char *) encodedSignal, outputArraySize);

	(*env)->ReleaseByteArrayElements(env,in,audioSignal,JNI_ABORT);
	(*env)->ReleaseByteArrayElements(env,out,encodedSignal,0);

	return dataArraySize;
}

JNIEXPORT jboolean JNICALL Java_com_score_rahasak_utils_OpusEncoder_nativeReleaseEncoder (JNIEnv *env, jobject obj)
{
	jclass cls = (*env)->GetObjectClass(env, obj);
	jfieldID fid = (*env)->GetFieldID(env, cls, "address", "J");
	OpusEncoder* enc = (OpusEncoder*)((*env)->GetLongField(env, obj, fid));
	free(enc);
	(*env)->SetLongField(env, obj, fid, (jlong)NULL);
    return 1;
}
