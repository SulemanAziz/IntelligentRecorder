# Intelligent Recorder

A smart video recording application for Android that uses motion detection to optimize storage and battery usage. The app intelligently records only frames where motion is detected, automatically stitching them together while discarding static frames.

## Features

- **Motion Detection**: Uses CameraX and computer vision to detect motion in real-time
- **Smart Recording**: Only captures and saves frames containing detected motion
- **Efficient Storage**: Significantly reduces video file sizes by excluding static content
- **Battery Optimized**: Reduces processing overhead by skipping motion-less frames
- **Real-time Preview**: Live camera feed with motion detection feedback
- **Easy to Use**: Simple, intuitive user interface

## How It Works

The application uses a three-step process:

1. **Capture**: CameraX captures video frames from the device camera
2. **Detect**: Motion detection algorithm analyzes each frame for movement
3. **Record**: Frames with detected motion are stitched together into a video file, while static frames are discarded

This approach is ideal for:
- Surveillance recordings
- Event-based video capture
- Reducing storage requirements for continuous monitoring
- Scenarios where you only care about movement in your recordings

## Screenshots

<img width="1080" height="2520" alt="AppSample" src="https://github.com/user-attachments/assets/52edbf94-9d67-4005-8fa4-beab353f646d" />

## Tech Stack

- **Kotlin** - Primary programming language
- **CameraX** - Camera functionality and frame capture
- **Android Architecture Components** - MVVM pattern and lifecycle management
- **OpenCV** or similar - Motion detection algorithm
- **FFmpeg** or MediaCodec - Video stitching and encoding

## Installation

### Prerequisites

- Android Studio (latest version)
- Android SDK 21+
- Gradle 7.0+

### Steps

1. Clone the repository:
```bash
git clone https://github.com/SulemanAziz/IntelligentRecorder.git
cd IntelligentRecorder
```

2. Open the project in Android Studio

3. Build the project:
```bash
./gradlew build
```

4. Run on an emulator or device:
```bash
./gradlew installDebug
```

## Usage

1. Launch the application
2. Grant camera and storage permissions
3. Point the camera at your subject
4. Tap the record button to start motion-based recording
5. The app will automatically detect motion and record only relevant frames
6. Tap stop to end recording
7. View your optimized video in the gallery

## API Permissions Required

- `android.permission.CAMERA` - Access device camera
- `android.permission.RECORD_AUDIO` - Record audio (if applicable)
- `android.permission.WRITE_EXTERNAL_STORAGE` - Save recordings
- `android.permission.READ_EXTERNAL_STORAGE` - Access saved files

## Algorithm

The motion detection algorithm works by:
- Comparing consecutive frames to identify changes
- Using threshold-based detection to filter noise
- Analyzing pixel differences across regions of interest
- Identifying significant movement patterns

*Detailed technical documentation coming soon.*

## Future Enhancements

- [ ] Adjustable motion sensitivity settings
- [ ] Multiple recording quality options
- [ ] Background blur and focus features
- [ ] Audio mixing and multi-track support
- [ ] Cloud storage integration
- [ ] Advanced motion analytics and heatmaps
- [ ] Custom motion detection regions
- [ ] Dark mode support

## Known Issues

*To be documented*

## Contributing

We welcome contributions! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Authors

- **Suleman Aziz** - *Initial work* - [SulemanAziz](https://github.com/SulemanAziz)

## Support

For support, please open an issue on the [GitHub Issues](https://github.com/SulemanAziz/IntelligentRecorder/issues) page.

## Acknowledgments

- CameraX for robust camera handling
- Android community for excellent documentation
- TBD - Additional references to be added

## References

*To be documented*
