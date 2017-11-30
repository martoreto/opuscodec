# opuscodec

Opus Audio Codec packaged for Android.

Based on code by λ.eranga as described in ["Opus codec with android VOIP application" post on Medium](https://medium.com/@itseranga/opus-codec-for-android-voip-application-7cfe7cd3dd9b),
with the following changes:

- Opus upgraded to 1.2.1
  - including ARM Neon and Intel SSE support
- extended encoder API
  - specify [profile](https://opus-codec.org/docs/opus_api-1.2/group__opus__encoder.html#gaa89264fd93c9da70362a0c9b96b9ca88)
  - set bitrate and complexity
- errors converted to exceptions

## Usage

Add this to your main _build.gradle_:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

and this to your app's _build.gradle_:

```gradle
dependencies {
    compile 'com.github.martoreto:opuscodec:v1.2.1.2'
}
```

and see [example app](example/), specifically [MainActivity.java](example/app/src/main/java/com/github/martoreto/opuscodecexample/MainActivity.java) for usage examples.
