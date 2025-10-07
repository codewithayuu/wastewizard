import { useRef, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image, Animated } from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import * as ImageManipulator from 'expo-image-manipulator';
import ResultSheet from '../components/scan/ResultSheet';

type ScanState = 'live' | 'analyzing' | 'result';
type Result = { item: string; category: string; confidence: number; co2eKg: number; instructions: string; prep: string[] };

// Mock analyze function (replace with real backend call later)
const mockAnalyze = async (): Promise<Result> => {
  await new Promise((r) => setTimeout(r, 1500));
  return {
    item: 'Plastic bottle',
    category: 'Recyclable',
    confidence: 0.87,
    co2eKg: 0.15,
    instructions: 'Rinse bottle, replace cap, and place in recycling bin.',
    prep: ['Rinse', 'Replace cap', 'Check recycling symbol'],
  };
};

export default function ScanScreen({ navigation }: any) {
  const cameraRef = useRef<CameraView>(null);
  const [permission, requestPermission] = useCameraPermissions();
  const [flash, setFlash] = useState(false);
  // Flashlight only supports two states: Off / On
  const [state, setState] = useState<ScanState>('live');
  const [preview, setPreview] = useState<string | null>(null);
  const [result, setResult] = useState<Result | null>(null);
  const fadeAnim = useRef(new Animated.Value(0)).current;

  const onCapture = async () => {
    if (!permission?.granted) {
      const granted = await requestPermission();
      if (!granted.granted) return;
    }
    try {
      setState('analyzing');
      const cam = cameraRef.current;
      if (!cam) return;
      const photo = await cam.takePictureAsync({ quality: 0.7, skipProcessing: true });
      const resized = await ImageManipulator.manipulateAsync(photo.uri, [{ resize: { width: 768 } }], { compress: 0.8, format: ImageManipulator.SaveFormat.JPEG });
      setPreview(resized.uri);

      const res = await mockAnalyze();
      setResult(res);
      setState('result');
      Animated.timing(fadeAnim, { toValue: 1, duration: 300, useNativeDriver: true }).start();
    } catch (e) {
      setState('live');
    }
  };

  const onGalleryImport = async () => {
    const r = await ImagePicker.launchImageLibraryAsync({ mediaTypes: 'images', quality: 0.8 });
    if (r.canceled) return;
    setState('analyzing');
    setPreview(r.assets[0].uri);
    const res = await mockAnalyze();
    setResult(res);
    setState('result');
    Animated.timing(fadeAnim, { toValue: 1, duration: 300, useNativeDriver: true }).start();
  };

  const onAddToLog = () => {
    // TODO: Save to log, update Home impact
    navigation.goBack();
  };

  const reset = () => {
    setState('live');
    setPreview(null);
    setResult(null);
    fadeAnim.setValue(0);
  };

  return (
    <View style={styles.container}>
      {/* Camera or Preview */}
      {state === 'live' && permission?.granted ? (
        <CameraView ref={cameraRef} style={styles.camera} facing="back" enableTorch={flash} />
      ) : state === 'live' && !permission?.granted ? (
        <View style={styles.center}>
          <Text style={styles.permText}>Camera permission required</Text>
          <TouchableOpacity style={styles.btn} onPress={() => requestPermission()}>
            <Text style={styles.btnText}>Grant permission</Text>
          </TouchableOpacity>
        </View>
      ) : (
        preview && <Image source={{ uri: preview }} style={styles.preview} />
      )}

      {/* Reticle Overlay */}
      {state === 'live' && <View style={styles.reticle} />}

      {/* Top Bar */}
      <View style={styles.topBar}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.iconBtn}>
          <Ionicons name="close" size={28} color="#fff" />
        </TouchableOpacity>
        <View style={{ flexDirection: 'row', gap: 8 }}>
          <TouchableOpacity onPress={() => setFlash(!flash)} style={styles.iconBtn}>
            <Ionicons name={flash ? 'flash' : 'flash-off'} size={24} color="#fff" />
          </TouchableOpacity>
          <TouchableOpacity style={styles.iconBtn}>
            <Ionicons name="help-circle-outline" size={24} color="#fff" />
          </TouchableOpacity>
        </View>
      </View>

      {/* Bottom Dock */}
      {state === 'live' && (
        <View style={styles.dock}>
          <TouchableOpacity onPress={onGalleryImport} style={styles.dockBtn}>
            <Ionicons name="images-outline" size={28} color="#fff" />
          </TouchableOpacity>
          <TouchableOpacity onPress={onCapture} style={styles.shutter}>
            <View style={styles.shutterInner} />
          </TouchableOpacity>
          <TouchableOpacity onPress={() => setFlash(!flash)} style={styles.dockBtn}>
            <Ionicons name={flash ? 'flash' : 'flash-off'} size={24} color="#fff" />
            <Text style={styles.dockText}>{flash ? 'On' : 'Off'}</Text>
          </TouchableOpacity>
        </View>
      )}

      {/* Analyzing Overlay */}
      {state === 'analyzing' && (
        <View style={styles.analyzing}>
          <Text style={styles.analyzingText}>Analyzing…</Text>
        </View>
      )}

      {/* Result Sheet */}
      {state === 'result' && result && (
        <Animated.View style={[styles.sheet, { opacity: fadeAnim }]}>
          <ResultSheet result={result} onAddToLog={onAddToLog} onRetake={reset} onClose={() => navigation.goBack()} />
        </Animated.View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#000' },
  camera: { flex: 1 },
  preview: { flex: 1, resizeMode: 'cover' },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 20 },
  permText: { fontSize: 16, color: '#fff', marginBottom: 12, textAlign: 'center' },
  btn: { backgroundColor: '#10b981', paddingHorizontal: 16, paddingVertical: 10, borderRadius: 8 },
  btnText: { color: '#fff', fontWeight: '600' },
  reticle: { position: 'absolute', alignSelf: 'center', top: '35%', width: 280, height: 280, borderWidth: 2, borderColor: '#10b981', borderRadius: 16, opacity: 0.8 },
  topBar: { position: 'absolute', top: 12, left: 12, right: 12, flexDirection: 'row', justifyContent: 'space-between', paddingHorizontal: 8, paddingVertical: 8, backgroundColor: 'rgba(0,0,0,0.3)', borderRadius: 12 },
  iconBtn: { alignItems: 'center', justifyContent: 'center', width: 40, height: 40 },
  dock: { position: 'absolute', bottom: 32, left: 0, right: 0, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 32 },
  dockBtn: { alignItems: 'center' },
  dockText: { fontSize: 10, color: '#fff', marginTop: 2 },
  shutter: { width: 72, height: 72, borderRadius: 36, backgroundColor: '#fff', alignItems: 'center', justifyContent: 'center', shadowColor: '#000', shadowOpacity: 0.3, shadowRadius: 12, shadowOffset: { width: 0, height: 4 }, elevation: 12 },
  shutterInner: { width: 60, height: 60, borderRadius: 30, backgroundColor: '#10b981' },
  analyzing: { position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.7)', alignItems: 'center', justifyContent: 'center' },
  analyzingText: { fontSize: 18, color: '#fff', fontWeight: '600' },
  sheet: { position: 'absolute', left: 0, right: 0, bottom: 0, maxHeight: '70%' },
});
