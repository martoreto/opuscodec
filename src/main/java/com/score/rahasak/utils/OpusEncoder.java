package com.score.rahasak.utils;

public class OpusEncoder {
    // Native pointer to OpusEncoder.
    private long address;

    public static final int OPUS_AUTO = -1;
    public static final int OPUS_BITRATE_MAX = -1;

    public static final int OPUS_COMPLEXITY_MAX = 10;

    private native int nativeInitEncoder(int samplingRate, int numberOfChannels);
    private native int nativeSetBitrate(int bitrate);
    private native int nativeSetComplexity(int complexity);
    private native int nativeEncodeShorts(short[] in, int frames, byte[] out);
    private native int nativeEncodeBytes(byte[] in, int frames, byte[] out);
    private native boolean nativeReleaseEncoder();

    static {
        System.loadLibrary("senz");
    }

    public void init(int sampleRate, int channels) {
        OpusError.throwIfError(this.nativeInitEncoder(sampleRate, channels));
    }

    public void setBitrate(int bitrate) {
        OpusError.throwIfError(this.nativeSetBitrate(bitrate));
    }

    public void setComplexity(int complexity) {
        OpusError.throwIfError(this.nativeSetComplexity(complexity));
    }

    public int encode(short[] buffer, int frames, byte[] out) {
        return OpusError.throwIfError(this.nativeEncodeShorts(buffer, frames, out));
    }

    public int encode(byte[] buffer, int frames, byte[] out) {
        return OpusError.throwIfError(this.nativeEncodeBytes(buffer, frames, out));
    }

    public void close() {
        this.nativeReleaseEncoder();
    }

}
