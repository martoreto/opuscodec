# opuscodec

Opus Audio Codec packaged for Android.

Based on code by λ.eranga as described in ["Opus codec with android VOIP application" post on Medium](https://medium.com/@itseranga/opus-codec-for-android-voip-application-7cfe7cd3dd9b),
with the following changes:

- Opus upgraded to 1.2.1
  - including ARM Neon and Intel SSE support
- extended encoder API
- errors converted to exceptions

## Usage

See [example app](example/), specifically [MainActivity.java](example/app/src/main/java/com/github/martoreto/opuscodecexample/MainActivity.java).
