import { useRef, useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image, Animated, Dimensions, Linking } from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import * as ImageManipulator from 'expo-image-manipulator';
import BottomSheet from '@gorhom/bottom-sheet';
import * as Haptics from 'expo-haptics';
import { useTheme } from '@react-navigation/native';
import { useAppStore } from '../store/appStore';
import ResultSheet from '../components/scan/ResultSheet';
import { predictGarbage } from '../api/client';

type ScanState = 'live' | 'analyzing' | 'result';
type Material = 'plastic' | 'paper' | 'glass' | 'metal' | 'organic' | 'e-waste' | 'hazardous';
type Result = { item: string; category: string; confidence: number; co2eKg: number; instructions: string; prep: string[]; material: Material };

const { height } = Dimensions.get('window');

function mapLabelToMaterial(label: string): Material {
  switch (label) {
    case 'plastic': return 'plastic';
    case 'paper': return 'paper';
    case 'glass': return 'glass';
    case 'metal': return 'metal';
    case 'cardboard': return 'paper';
    case 'trash': return 'hazardous';
    default: return 'plastic';
  }
}

function buildResult(label: string, confidence: number, uri: string): Result {
  const material = mapLabelToMaterial(label);
  const pretty = label.charAt(0).toUpperCase() + label.slice(1);
  const guidance: Record<Material, { instructions: string; prep: string[]; co2eKg: number; category: string }> = {
    plastic: { instructions: 'Rinse, remove liquids, cap back on, recycle if accepted.', prep: ['Rinse', 'Dry', 'Cap on'], co2eKg: 0.15, category: 'Recyclable' },
    paper: { instructions: 'Keep dry and clean. Flatten before recycling.', prep: ['Flatten', 'Remove tape'], co2eKg: 0.08, category: 'Recyclable' },
    glass: { instructions: 'Rinse and place in glass recycling bin if accepted.', prep: ['Rinse'], co2eKg: 0.2, category: 'Recyclable' },
    metal: { instructions: 'Rinse cans, remove labels if possible.', prep: ['Rinse', 'Crush lightly'], co2eKg: 0.25, category: 'Recyclable' },
    organic: { instructions: 'Compost if available; otherwise dispose as per local rules.', prep: ['Remove packaging'], co2eKg: 0.05, category: 'Compostable' },
    'e-waste': { instructions: 'Do not bin. Take to an e-waste drop-off.', prep: ['Remove batteries'], co2eKg: 0.0, category: 'Special disposal' },
    hazardous: { instructions: 'Do not bin. Follow local hazardous waste guidelines.', prep: ['Seal safely'], co2eKg: 0.0, category: 'Special disposal' },
  };
  const g = guidance[material];
  return { item: pretty, category: g.category, confidence, co2eKg: g.co2eKg, instructions: g.instructions, prep: g.prep, material };
}

export default function ScanScreen({ navigation }: any) {
  const { colors, dark } = useTheme();
  const cameraRef = useRef<CameraView>(null);
  const sheetRef = useRef<BottomSheet>(null);
  const [permission, requestPermission] = useCameraPermissions();
  const [flash, setFlash] = useState(false);
  const [state, setState] = useState<ScanState>('live');
  const [preview, setPreview] = useState<string | null>(null);
  const [result, setResult] = useState<Result | null>(null);
  const imageHeight = useRef(new Animated.Value(height * 0.65)).current;

  // Auto prompt once if we can ask again
  useEffect(() => {
    if (permission && !permission.granted && permission.canAskAgain) {
      requestPermission();
    }
  }, [permission]);

  const onGrantPermission = async () => {
    const res = await requestPermission();
    if (!res.granted && !res.canAskAgain) {
      Linking.openSettings();
    }
  };

  const onCapture = async () => {
    if (!permission?.granted) {
      const granted = await requestPermission();
      if (!granted.granted) return;
    }
    try {
      setState('analyzing');
      const cam = cameraRef.current;
      if (!cam) return;
      const photo = await cam.takePictureAsync({ quality: 0.7 });
      if (!photo) return;
      const resized = await ImageManipulator.manipulateAsync(photo.uri, [{ resize: { width: 768 } }], { compress: 0.8, format: ImageManipulator.SaveFormat.JPEG });
      setPreview(resized.uri);
      sheetRef.current?.snapToIndex(1);
      try {
        const pred = await predictGarbage(resized.uri);
        const built = buildResult(pred.class, pred.confidence, resized.uri);
        setResult(built);
      } catch (err) {
        // prediction failed, fallback to local guidance
        const built = buildResult('plastic', 0.5, resized.uri);
        setResult(built);
      }
      setState('result');
      const achievementsOn = useAppStore.getState().settings.notifications.achievements;
      if (achievementsOn) {
        Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      }
      sheetRef.current?.snapToIndex(1);
    } catch (e) {
      // capture failed
      setState('live');
    }
  };

  const onGalleryImport = async () => {
    const r = await ImagePicker.launchImageLibraryAsync({ mediaTypes: 'images', quality: 0.8 });
    if (r.canceled) return;
    setState('analyzing');
    setPreview(r.assets[0].uri);
    sheetRef.current?.snapToIndex(1);
    try {
      const pred = await predictGarbage(r.assets[0].uri);
      const built = buildResult(pred.class, pred.confidence, r.assets[0].uri);
      setResult(built);
    } catch (err) {
      // prediction failed, fallback
      const built = buildResult('plastic', 0.5, r.assets[0].uri);
      setResult(built);
    }
    setState('result');
    sheetRef.current?.snapToIndex(1);
  };

  const onAddToLog = () => {
    if (!result) return;
    const addActivity = useAppStore.getState().addActivity;
    const addPoints = useAppStore.getState().addPoints;
    addActivity({ id: Date.now().toString(), type: 'scan', title: result.item, material: result.material, when: 'Just now', points: 10, thumb: preview || undefined });
    addPoints(10, result.material);
    navigation.goBack();
  };

  const reset = () => {
    setState('live');
    setPreview(null);
    setResult(null);
    sheetRef.current?.close();
    Animated.timing(imageHeight, { toValue: height * 0.65, duration: 200, useNativeDriver: false }).start();
  };

  const onSheetChange = (index: number) => {
    const targetHeight = index === 0 ? height * 0.65 : index === 1 ? height * 0.45 : height * 0.3;
    Animated.timing(imageHeight, { toValue: targetHeight, duration: 200, useNativeDriver: false }).start();
  };

  return (
    <View style={styles.container}>
      <Animated.View style={{ height: imageHeight, width: '100%' }}>
        {state === 'live' && permission?.granted ? (
          <CameraView ref={cameraRef} style={styles.camera} facing="back" enableTorch={flash} />
        ) : state === 'live' && !permission?.granted ? (
          <View style={styles.center}>
            <Text style={styles.permText}>Camera permission required</Text>
            <TouchableOpacity style={styles.btn} onPress={onGrantPermission}>
              <Text style={styles.btnText}>Grant permission</Text>
            </TouchableOpacity>
          </View>
        ) : preview ? (
          <Image source={{ uri: preview }} style={styles.preview} />
        ) : null}

        {state === 'live' && <View style={styles.reticle} />}

        <View style={styles.topBar}>
          <TouchableOpacity onPress={() => navigation.goBack()} style={styles.iconBtn}>
            <Ionicons name="close" size={28} color="#fff" />
          </TouchableOpacity>
        </View>

        <View style={styles.dock}>
          <TouchableOpacity onPress={onGalleryImport} style={styles.dockBtn}>
            <Ionicons name="images-outline" size={28} color="#fff" />
          </TouchableOpacity>
          <TouchableOpacity onPress={onCapture} style={styles.shutter} disabled={!permission?.granted}>
            <View style={styles.shutterInner} />
          </TouchableOpacity>
          <TouchableOpacity onPress={() => setFlash(!flash)} style={styles.dockBtn}>
            <Ionicons name={flash ? 'flash' : 'flash-off'} size={24} color="#fff" />
            <Text style={styles.dockText}>{flash ? 'On' : 'Off'}</Text>
          </TouchableOpacity>
        </View>

        {/* Analyzing skeleton moves to BottomSheet */}
      </Animated.View>

      {(state === 'analyzing' || (state === 'result' && result)) && (
        <BottomSheet
          ref={sheetRef}
          index={0}
          snapPoints={['18%', '55%', '92%']}
          enablePanDownToClose
          onClose={reset}
          onChange={onSheetChange}
          backgroundStyle={{ backgroundColor: dark ? 'rgba(14,17,22,0.98)' : 'rgba(255,255,255,0.98)', borderTopLeftRadius: 24, borderTopRightRadius: 24 }}
          handleIndicatorStyle={{ backgroundColor: dark ? '#4B5563' : '#CBD5E1' }}
        >
          {state === 'analyzing' ? (
            <View style={{ padding: 16 }}>
              <Text style={{ color: dark ? '#E5E7EB' : '#111827', fontWeight: '600', marginBottom: 8 }}>Analyzing…</Text>
              <View style={{ height: 8, borderRadius: 999, backgroundColor: dark ? '#1F2937' : '#E5E7EB', overflow: 'hidden' }}>
                <View style={{ width: '55%', height: 8, backgroundColor: colors.primary, borderRadius: 999 }} />
              </View>
            </View>
          ) : (
            result && (
              <ResultSheet result={result} onAddToLog={onAddToLog} onRetake={reset} onClose={() => navigation.goBack()} />
            )
          )}
        </BottomSheet>
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
  topBar: { position: 'absolute', top: 12, left: 12, right: 12, paddingHorizontal: 8, paddingVertical: 8 },
  iconBtn: { alignItems: 'center', justifyContent: 'center', width: 40, height: 40 },
  dock: { position: 'absolute', bottom: 32, left: 0, right: 0, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 32 },
  dockBtn: { alignItems: 'center' },
  dockText: { fontSize: 10, color: '#fff', marginTop: 2 },
  shutter: { width: 72, height: 72, borderRadius: 36, backgroundColor: '#fff', alignItems: 'center', justifyContent: 'center', shadowColor: '#000', shadowOpacity: 0.3, shadowRadius: 12, shadowOffset: { width: 0, height: 4 }, elevation: 12 },
  shutterInner: { width: 60, height: 60, borderRadius: 30, backgroundColor: '#10b981' },
  analyzing: { position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.7)', alignItems: 'center', justifyContent: 'center' },
  analyzingText: { fontSize: 18, color: '#fff', fontWeight: '600' },
});
