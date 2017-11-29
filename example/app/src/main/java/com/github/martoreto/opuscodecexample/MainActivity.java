package com.github.martoreto.opuscodecexample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.score.rahasak.utils.OpusDecoder;
import com.score.rahasak.utils.OpusEncoder;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    Button mStartButton;
    Button mStopButton;
    AudioThread mAudioThread = new AudioThread();
    private boolean mIsStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartButton = findViewById(R.id.start_button);
        mStopButton = findViewById(R.id.stop_button);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
            }
        });
    }

    private void start() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        mIsStarted = true;

        mStartButton.setEnabled(false);
        mStopButton.setEnabled(true);
        mAudioThread = new AudioThread();
        mAudioThread.start();
    }

    private void stop() {
        mAudioThread.interrupt();
        try {
            mAudioThread.join();
        } catch (InterruptedException e) {
            Log.w(TAG, "Interrupted waiting for audio thread to finish");
        }
        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);

        mIsStarted = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mIsStarted) {
            stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    start();
                }
            }
        }
    }

    private class AudioThread extends Thread {
        // Sample rate must be one supported by Opus.
        static final int SAMPLE_RATE = 8000;

        // Number of samples per frame is not arbitrary,
        // it must match one of the predefined values, specified in the standard.
        static final int FRAME_SIZE = 160;

        // 1 or 2
        static final int NUM_CHANNELS = 1;

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

            int minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT);

            // initialize audio recorder
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize);

            // init opus encoder
            OpusEncoder encoder = new OpusEncoder();
            encoder.init(SAMPLE_RATE, NUM_CHANNELS, OpusEncoder.OPUS_APPLICATION_VOIP);

            // init audio track
            AudioTrack track = new AudioTrack(AudioManager.STREAM_SYSTEM,
                    SAMPLE_RATE,
                    NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize,
                    AudioTrack.MODE_STREAM);

            // init opus decoder
            OpusDecoder decoder = new OpusDecoder();
            decoder.init(SAMPLE_RATE, NUM_CHANNELS);

            // start
            recorder.startRecording();
            track.play();

            byte[] inBuf = new byte[FRAME_SIZE * NUM_CHANNELS * 2];
            byte[] encBuf = new byte[1024];
            short[] outBuf = new short[FRAME_SIZE * NUM_CHANNELS];

            try {
                while (!Thread.interrupted()) {
                    // Encoder must be fed entire frames.
                    int to_read = inBuf.length;
                    int offset = 0;
                    while (to_read > 0) {
                        int read = recorder.read(inBuf, offset, to_read);
                        if (read < 0) {
                            throw new RuntimeException("recorder.read() returned error " + read);
                        }
                        to_read -= read;
                        offset += read;
                    }

                    int encoded = encoder.encode(inBuf, FRAME_SIZE, encBuf);

                    Log.v(TAG, "Encoded " + inBuf.length + " bytes of audio into " + encoded + " bytes");

                    byte[] encBuf2 = Arrays.copyOf(encBuf, encoded);

                    int decoded = decoder.decode(encBuf2, outBuf, FRAME_SIZE);

                    Log.v(TAG, "Decoded back " + decoded * NUM_CHANNELS * 2 + " bytes");

                    track.write(outBuf, 0, decoded * NUM_CHANNELS);
                }
            } finally {
                recorder.stop();
                recorder.release();
                track.stop();
                track.release();
            }
        }
    }
}
