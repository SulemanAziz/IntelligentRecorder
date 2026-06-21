# Intelligent Recorder

A smart video recording application for Android that uses motion detection to optimize storage and battery usage. The app intelligently records only frames where motion is detected, automatically stitching them together while discarding static frames.

## Features

- **Multi-Mode Motion Detection**: Three distinct detection modes for different use cases
  - **Foreground Mode**: Full-frame motion detection for general-purpose recording
  - **Mirror Mode**: Region-specific detection with customizable mirror area
  - **Hybrid Mode**: Simultaneous detection in both mirror and foreground regions with independent thresholds
- **Smart Recording**: Only captures and saves frames containing detected motion
- **Customizable Sensitivity**: Independent threshold controls for each detection mode
- **Region-Based Detection**: Define custom detection zones for precise motion tracking
- **Temporal Filtering**: Advanced noise reduction to eliminate false positives
- **Dual Motion Indicators**: Visual feedback for mirror and foreground motion in Hybrid mode
- **Efficient Storage**: Significantly reduces video file sizes by excluding static content
- **Battery Optimized**: Reduces processing overhead by skipping motion-less frames
- **Real-time Preview**: Live camera feed with motion detection feedback
- **Easy to Use**: Simple, intuitive user interface with mode switching

## How It Works

The application uses an advanced multi-mode motion detection system:

1. **Capture**: CameraX captures video frames from the device camera
2. **Analyze**: Motion detection algorithm processes frames based on selected mode
3. **Detect**: 
   - **Foreground Mode**: Analyzes the entire frame for motion using full-frame threshold
   - **Mirror Mode**: Focuses on a user-defined region (e.g., rearview mirror) with dedicated sensitivity
   - **Hybrid Mode**: Monitors both mirror region and full frame simultaneously with independent thresholds
4. **Filter**: Temporal filtering reduces false positives by requiring motion detection across consecutive frames
5. **Record**: Frames with detected motion are stitched together into a video file, while static frames are discarded

### Detection Modes

#### Foreground Mode
Ideal for general-purpose recording where you want to capture any movement in the entire scene.
- Full-frame motion analysis
- Single threshold control
- Best for dashcam, surveillance, and event recording

#### Mirror Mode
Perfect for focused monitoring of specific areas like a rearview mirror in vehicles.
- User-defined detection region with four-point selection
- Dedicated mirror threshold for fine-tuned sensitivity
- Ignores motion outside the defined region
- Coordinate transformation handles sensor rotation automatically

#### Hybrid Mode
Advanced mode that monitors both regions independently with separate visual indicators.
- Simultaneous detection in mirror region and full frame
- Independent threshold controls for each region
- Dual motion indicators (Cyan for mirror, Magenta for foreground)
- Records when motion is detected in either or both regions

This approach is ideal for:
- Dashcam recordings with rearview mirror monitoring
- Surveillance with focus on specific entry points
- Event-based video capture with region prioritization
- Reducing storage requirements while maintaining comprehensive monitoring
- Scenarios requiring different sensitivity levels for different areas

## Screenshots

<img width="1080" height="2520" alt="AppSample" src="https://github.com/user-attachments/assets/52edbf94-9d67-4005-8fa4-beab353f646d" />

## Tech Stack

- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern declarative UI framework
- **CameraX** - Camera functionality and frame capture
- **Android Architecture Components** - MVVM pattern and lifecycle management
- **Kotlin Coroutines & Flow** - Asynchronous programming and reactive state management
- **MediaCodec** - Video encoding and processing
- **Computer Vision** - Y-plane luminance analysis for motion detection

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
2. Grant camera and storage permissions when prompted
3. **Select Detection Mode** from the dropdown menu:
   - **Foreground**: For general-purpose full-frame recording
   - **Mirror**: To focus on a specific region
   - **Hybrid**: To monitor both regions simultaneously
4. **Configure Settings** (tap the gear icon):
   - Adjust motion sensitivity thresholds (0-100%)
   - In Hybrid mode, set independent thresholds for mirror and foreground
5. **Set Mirror Region** (Mirror/Hybrid modes only):
   - Tap the mirror configuration button
   - Select four points to define your detection region
   - Region adapts to camera rotation automatically
6. Point the camera at your subject
7. Tap the record button to start motion-based recording
8. **Monitor Motion Indicators**:
   - Single yellow icon in Foreground/Mirror modes
   - Dual icons (Cyan + Magenta) in Hybrid mode show region-specific motion
9. The app will automatically detect motion and record only relevant frames
10. Tap stop to end recording
11. Save or discard the recording using on-screen controls
12. View your optimized video in the gallery

### Tips for Best Results
- **Foreground Mode**: Use 3-5% threshold for general scenes, increase for busy environments
- **Mirror Mode**: Use 5-8% threshold for focused detection, adjust based on region size
- **Hybrid Mode**: Start with 5% foreground and 8% mirror, fine-tune based on your needs
- **Mounting**: Stable mounting reduces false positives from camera shake
- **Lighting**: Consistent lighting improves detection accuracy

## API Permissions Required

- `android.permission.CAMERA` - Access device camera
- `android.permission.RECORD_AUDIO` - Record audio (if applicable)
- `android.permission.WRITE_EXTERNAL_STORAGE` - Save recordings
- `android.permission.READ_EXTERNAL_STORAGE` - Access saved files

## Algorithm

The motion detection algorithm uses advanced computer vision techniques:

### Core Detection Process
- **Y-Plane Luminance Analysis**: Extracts and compares luminance values from consecutive frames
- **Pixel Difference Threshold**: Uses a 30-unit threshold to filter sensor noise and lighting variations
- **Percentage-Based Motion**: Calculates percentage of changed pixels against configurable thresholds
- **Adaptive Sampling**: Samples every 4th pixel for performance optimization without sacrificing accuracy

### Temporal Filtering
- **Consecutive Frame Requirement**: Motion must be detected in 2+ consecutive frames to trigger
- **False Positive Reduction**: Eliminates single-frame noise from sensor artifacts or lighting changes
- **Smooth Transitions**: Gradual motion state changes prevent flickering indicators

### Region-Based Detection (Mirror & Hybrid Modes)
- **Coordinate Transformation**: Converts normalized display coordinates (0..1) to sensor pixel space
- **Rotation Handling**: Accounts for 0°, 90°, 180°, 270° sensor-to-display rotation
- **Bounding Box Optimization**: Analyzes only pixels within the defined region for efficiency
- **Four-Point Selection**: User-defined quadrilateral region for precise area targeting

### Multi-Mode Architecture
- **Independent Thresholds**: Each mode maintains separate sensitivity controls
- **Dual Indicators**: Hybrid mode provides real-time feedback for both regions
- **Mode Persistence**: Settings preserved across mode switches for quick comparison

## Future Enhancements

- [x] Multiple detection modes (Foreground, Mirror, Hybrid)
- [x] Adjustable motion sensitivity settings with mode-specific thresholds
- [x] Custom motion detection regions
- [x] Temporal filtering to reduce false positives
- [ ] Multiple recording quality options
- [ ] Adjustable temporal filter sensitivity
- [ ] Polygon-based detection regions (beyond quadrilaterals)
- [ ] Motion heatmap visualization
- [ ] Historical motion analytics and statistics
- [ ] Background blur and focus features
- [ ] Audio mixing and multi-track support
- [ ] Cloud storage integration
- [ ] Export detection regions as presets
- [ ] Dark mode support
- [ ] Multi-region detection (more than 2 zones)

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
