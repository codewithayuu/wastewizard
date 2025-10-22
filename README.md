# WasteWizard

An intelligent Android application for waste classification using TensorFlow Lite machine learning. WasteWizard helps users identify different types of waste materials and provides personalized tips for reducing and recycling.

## 🌟 Features

### Core Functionality
- **AI-Powered Classification**: Uses TensorFlow Lite to classify waste into 5 categories:
  - 🟦 Plastic
  - 🟩 Glass  
  - ⚪ Metal
  - 🟫 Paper
  - 🟠 Cardboard

### Advanced Features
- **Real-time Classification**: Live camera preview with throttled AI analysis
- **Auto-classify**: Automatic classification after capture/pick
- **Yes/No Confirmation**: User feedback system for gamification
- **Scan History**: Complete history of all classifications with timestamps
- **Gamification**: Points, levels, streaks, and achievements system
- **Settings Screen**: Username, theme preferences, and app configuration

### User Interface
- **Material Design 3**: Modern, intuitive UI following Google's design guidelines
- **Dynamic Colors**: Material You integration on Android 12+
- **Dark/Light Theme**: Automatic theme switching with manual toggle option
- **Edge-to-Edge Display**: Modern full-screen experience with proper insets
- **Responsive Layout**: Optimized for various screen sizes
- **Loading Indicators**: Visual feedback during AI processing

### Image Input
- **CameraX Integration**: Modern camera API with in-app preview
- **Photo Picker**: Android 13+ native image picker
- **Gallery Selection**: Fallback for older Android versions
- **Permission Handling**: Smart camera permission management with rationale dialogs

### Educational Content
- **Reduce Tips**: Personalized advice for minimizing waste
- **Recycle Guidelines**: Specific recycling instructions for each material type
- **Color-Coded Results**: Visual indicators for different waste categories
- **Category Colors**: Harmonized colors that blend with dynamic themes

### Gamification System
- **Points System**: Earn points for correct classifications
- **Level Progression**: Level up based on total points
- **Streak Tracking**: Maintain classification streaks
- **Achievements**: Unlock achievements for milestones
- **Leaderboard**: Compare progress with other users
- **Statistics**: Today's scans, weekly progress, accuracy tracking

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version recommended)
- Android device or emulator with API level 21+ (Android 5.0)
- Camera permission for photo capture functionality

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/codewithayuu/wastewizard
   cd wastewizard
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory

3. **Sync and Build**
   - Wait for Gradle sync to complete
   - Build the project (Build → Make Project)
   - Run on device or emulator

### First Run
1. Grant camera permission when prompted
2. Set your username in Settings
3. Take a photo or select from gallery
4. Confirm classification accuracy (Yes/No)
5. View your progress in Dashboard and History

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/example/wastewizard/
│   │   ├── MainAppActivity.java          # Main activity with navigation
│   │   ├── ScanFragment.java             # Camera and classification logic
│   │   ├── DashboardFragment.java        # Stats and quick actions
│   │   ├── HistoryFragment.java          # Scan history display
│   │   ├── ProfileFragment.java          # User profile and achievements
│   │   ├── LeaderboardFragment.java     # User rankings
│   │   ├── SettingsFragment.java         # App settings and preferences
│   │   ├── AboutFragment.java            # App information and licenses
│   │   ├── TFLiteClassifier.java         # TensorFlow Lite integration
│   │   ├── GameManager.java              # Gamification system
│   │   ├── AppThemeManager.java          # Theme and settings management
│   │   ├── CategoryColors.java           # Dynamic color harmonization
│   │   └── WasteWizardApp.java           # Application class
│   ├── assets/
│   │   ├── garbage_model.tflite          # Pre-trained ML model
│   │   ├── labels.txt                    # Model classification labels
│   │   └── model_version.txt             # Model version information
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main_app.xml     # Main activity layout
│   │   │   ├── fragment_scan.xml         # Scan screen layout
│   │   │   ├── fragment_dashboard.xml   # Dashboard layout
│   │   │   ├── fragment_history.xml     # History layout
│   │   │   ├── fragment_profile.xml     # Profile layout
│   │   │   ├── fragment_leaderboard.xml # Leaderboard layout
│   │   │   ├── fragment_settings.xml    # Settings layout
│   │   │   ├── fragment_about.xml       # About layout
│   │   │   └── item_*.xml               # RecyclerView item layouts
│   │   ├── values/
│   │   │   ├── colors.xml                # Color definitions
│   │   │   ├── strings.xml               # String resources
│   │   │   ├── themes.xml                # App themes
│   │   │   ├── styles.xml                # Custom styles
│   │   │   └── dimens.xml                # Spacing and dimensions
│   │   ├── values-night/
│   │   │   └── themes.xml                # Dark theme overrides
│   │   ├── menu/
│   │   │   ├── bottom_nav_menu.xml       # Bottom navigation
│   │   │   └── main_app_menu.xml         # Toolbar menu
│   │   ├── xml/
│   │   │   └── prefs_settings.xml        # Settings preferences
│   │   └── raw/
│   │       └── licenses.html             # Open source licenses
│   └── AndroidManifest.xml               # App configuration
└── build.gradle                          # Dependencies and build config
```

## 🛠 Technical Details

### Machine Learning
- **Framework**: TensorFlow Lite 2.16.1+
- **Model**: Custom-trained CNN for waste classification
- **Input**: 180x180 RGB images with YUV_420_888 conversion
- **Output**: 5-class classification (cardboard, glass, metal, paper, plastic)
- **Processing**: On-device inference for privacy and speed
- **Real-time**: Throttled analysis at ~2.5 FPS for live preview

### Architecture
- **UI Framework**: Material Design Components 1.12.0+
- **Navigation**: Fragment-based navigation with bottom navigation
- **Image Processing**: CameraX for camera, Photo Picker for gallery
- **Async Processing**: Background threads for ML inference
- **Memory Management**: Efficient bitmap handling and model cleanup
- **Theme System**: Dynamic colors with Material You integration

### Permissions
- `CAMERA`: Required for photo capture functionality
- `READ_MEDIA_IMAGES`: For Android 13+ gallery access
- `READ_EXTERNAL_STORAGE`: Fallback for older Android versions

### Recent Updates

#### v2.0.0 - Major Feature Update
- ✅ **Real-time Classification**: Live camera preview with AI analysis
- ✅ **Gamification System**: Points, levels, streaks, and achievements
- ✅ **Settings Screen**: Username, theme preferences, and app configuration
- ✅ **Dynamic Colors**: Material You integration on Android 12+
- ✅ **Edge-to-Edge**: Modern full-screen experience
- ✅ **Yes/No Confirmation**: User feedback system for accurate stats
- ✅ **Scan History**: Complete history with timestamps and accuracy tracking
- ✅ **Material Design 3**: Updated UI with latest design guidelines

#### v1.5.0 - Stability Improvements
- ✅ **CameraX Migration**: Modern camera API with in-app preview
- ✅ **Permission Handling**: Smart permission management with rationale dialogs
- ✅ **Fragment Lifecycle**: Proper lifecycle management for stability
- ✅ **Memory Optimization**: Efficient bitmap handling and cleanup
- ✅ **Error Handling**: Comprehensive error handling and user feedback

#### v1.0.0 - Initial Release
- ✅ **Basic Classification**: TensorFlow Lite waste classification
- ✅ **Camera Integration**: Photo capture and gallery selection
- ✅ **Material Design**: Modern UI with dark/light themes
- ✅ **Educational Content**: Tips and guidelines for waste management

## 🎨 Customization

### Adding New Waste Categories
1. Retrain the TensorFlow model with additional classes
2. Update `labels.txt` with new category names
3. Add corresponding colors in `colors.xml`
4. Update tip generation methods in `ScanFragment.java`

### UI Customization
- Modify `colors.xml` for different color schemes
- Update `themes.xml` for custom Material Design themes
- Adjust layouts in fragment XML files
- Customize spacing in `dimens.xml`

### Gamification Customization
- Modify point values in `GameManager.java`
- Add new achievements in the achievements system
- Customize level progression requirements
- Update leaderboard scoring algorithms

## 🙏 Acknowledgments

- TensorFlow Lite team for the ML framework
- Material Design team for the UI components
- Android development community for best practices
- CameraX team for modern camera APIs
- Material You team for dynamic color system

---

**WasteWizard** - Making waste management smarter, one classification at a time! 🌍♻️

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.


### Installation Instructions
1. Enable "Install from unknown sources" in device settings
2. Navigate to Downloads folder
3. Tap `WasteWizard.apk` to install
4. Launch the app and grant camera permission
5. Start classifying waste and earning points!