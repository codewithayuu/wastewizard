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

### User Interface
- **Material Design 3**: Modern, intuitive UI following Google's design guidelines
- **Dark/Light Theme**: Automatic theme switching with manual toggle option
- **Responsive Layout**: Optimized for various screen sizes
- **Loading Indicators**: Visual feedback during AI processing

### Image Input
- **Camera Capture**: Take photos directly within the app
- **Gallery Picker**: Select images from device gallery
- **Permission Handling**: Smart camera permission management

### Educational Content
- **Reduce Tips**: Personalized advice for minimizing waste
- **Recycle Guidelines**: Specific recycling instructions for each material type
- **Color-Coded Results**: Visual indicators for different waste categories

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
2. Take a photo or select from gallery
3. Tap "Predict" to analyze the waste material
4. View classification results and tips

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/example/wastewizard/
│   │   ├── MainActivity.java          # Main activity with UI logic
│   │   └── TFLiteClassifier.java      # TensorFlow Lite integration
│   ├── assets/
│   │   ├── garbage_model.tflite       # Pre-trained ML model
│   │   └── labels.txt                 # Model classification labels
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml      # Main UI layout
│   │   ├── values/
│   │   │   ├── colors.xml             # Color definitions
│   │   │   ├── strings.xml            # String resources
│   │   │   └── themes.xml             # App themes
│   │   ├── menu/
│   │   │   └── menu_main.xml          # Options menu
│   │   └── xml/
│   │       └── file_paths.xml         # File provider configuration
│   └── AndroidManifest.xml            # App configuration
└── build.gradle                       # Dependencies and build config
```

## 🛠 Technical Details

### Machine Learning
- **Framework**: TensorFlow Lite 2.12.0
- **Model**: Custom-trained CNN for waste classification
- **Input**: 180x180 RGB images
- **Output**: 5-class classification (cardboard, glass, metal, paper, plastic)
- **Processing**: On-device inference for privacy and speed

### Architecture
- **UI Framework**: Material Design Components
- **Image Processing**: Android CameraX and MediaStore
- **Async Processing**: Background threads for ML inference
- **Memory Management**: Efficient bitmap handling and model cleanup

### Permissions
- `CAMERA`: Required for photo capture functionality
- File provider configured for secure image sharing

## 🎨 Customization

### Adding New Waste Categories
1. Retrain the TensorFlow model with additional classes
2. Update `labels.txt` with new category names
3. Add corresponding colors in `colors.xml`
4. Update tip generation methods in `MainActivity.java`

### UI Customization
- Modify `colors.xml` for different color schemes
- Update `themes.xml` for custom Material Design themes
- Adjust layouts in `activity_main.xml`

The app features:
- Clean, intuitive interface with Material Design 3
- Real-time image preview
- Instant AI-powered waste classification
- Educational tips with color-coded categories
- Dark/light theme support

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request


## 🙏 Acknowledgments

- TensorFlow Lite team for the ML framework
- Material Design team for the UI components
- Android development community for best practices

---

**WasteWizard** - Making waste management smarter, one classification at a time! 🌍♻️
---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.