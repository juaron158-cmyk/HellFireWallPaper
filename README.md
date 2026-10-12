# HellFire Wallpaper

A live wallpaper for Android that recreates the fire effect from DOOM PSX, featuring adjustment controls and the option to use your own background image.

## Features

- Fire simulation based on the classic DOOM algorithm.
- Adjustable color (hue), flame height, and wind (left or right).
- Real-time adjustable FPS and graininess (fire cell size).
- Background image from your gallery, with zoom and pan for cropping.
- Touch-reactive fire (optional).
- Spanish and English interface, with an in-app language selector.

## Permissions

- `SET_WALLPAPER`: to apply the live wallpaper.
- `READ_MEDIA_IMAGES` (Android 13 or higher) and `READ_EXTERNAL_STORAGE` (Android 12 or lower): to read your selected background image.

The app does not use the internet, contains no ads, and collects no data.

## Building

1. Clone the repository.
2. Open it in Android Studio.
3. Run Build → Build Bundle(s) / APK(s) → Build APK(s), or run the app on a device or emulator.

Requires a recent version of Android Studio and an SDK compatible with the `minSdk` defined in `app/build.gradle.kts`.

## License

This project is free software distributed under the GPL-3.0-or-later license. See the [LICENSE](LICENSE) file for the full text.

## Credits

See [CREDITS.md](CREDITS.md).

This project is not affiliated with id Software, Bethesda, Microsoft, ZeniMax, or Sony.
