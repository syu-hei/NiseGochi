# NiseGochi

NiseGochi is a modern, adaptive retro pet simulation app built with Jetpack Compose.

## Features

- **Retro Aesthetic**: Authentic pixel art and LCD-style display.
- **Adaptive UI**: Works seamlessly on phones, tablets, and foldable devices using Material 3 Adaptive.
- **Deep Simulation**: Parity with classic pet mechanics, including hunger, happiness, evolution, and death.
- **Notifications**: Stay updated on your pet's needs with background worker integration.
- **Interactive Audio**: Authentic sound effects for a nostalgic experience.

## Tech Stack

- **UI**: Jetpack Compose, Material 3, Navigation 3.
- **Architecture**: MVVM with StateFlow and ViewModel.
- **Persistence**: Room Database.
- **Background Tasks**: WorkManager.
- **Media**: Coil for images, MediaPlayer for audio.

## Getting Started

1. Open the project in Android Studio.
2. Build and run the `app` module.
3. Name your pet and start the simulation!

## Development

- Use the `Debug Mode` by naming your pet "DEBUG" or pressing the clock "C" button 10 times.
- Run tests via `./gradlew test`.
