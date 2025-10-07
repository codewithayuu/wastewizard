import { useRef, useState } from 'react';
import { View, Text, StyleSheet, Button, Image, ActivityIndicator } from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import * as ImageManipulator from 'expo-image-manipulator';
import * as FileSystem from 'expo-file-system';
import { infer } from '../api';

export default function ScanScreen({ navigation }: any) {
  const cameraRef = useRef<CameraView>(null);
  const [permission, requestPermission] = useCameraPermissions();
  const [previewUri, setPreviewUri] = useState<string | null>(null);
  const [isCapturing, setIsCapturing] = useState(false);
  const [result, setResult] = useState<any>(null);

  const capture = async () => {
    if (!permission?.granted) {
      const granted = await requestPermission();
      if (!granted.granted) return;
    }
    try {
      setIsCapturing(true);
      const cam = cameraRef.current;
      if (!cam) return;
      const photo = await cam.takePictureAsync({ skipProcessing: true, quality: 0.9, base64: true });
      const resized = await ImageManipulator.manipulateAsync(photo.uri, [{ resize: { width: 768 } }], { compress: 0.8, format: ImageManipulator.SaveFormat.JPEG });
      setPreviewUri(resized.uri);
      let base64 = (photo as any).base64 as string | undefined;
      if (!base64) base64 = await FileSystem.readAsStringAsync(resized.uri, { encoding: FileSystem.EncodingType.Base64 });
      const res = await infer(base64);
      setResult(res);
    } finally {
      setIsCapturing(false);
    }
  };

  return (
    <View style={styles.container}>
      {!previewUri ? (
        <View style={{ flex: 1 }}>
          <CameraView ref={cameraRef} style={{ flex: 1 }} />
          <View style={{ padding: 12 }}>
            <Button title={isCapturing ? 'Capturing…' : 'Scan'} onPress={capture} disabled={isCapturing} />
            <Button title="Close" onPress={() => navigation.goBack()} />
          </View>
        </View>
      ) : (
        <View style={{ flex: 1, padding: 12 }}>
          <Image source={{ uri: previewUri }} style={{ width: '100%', height: 300, borderRadius: 8 }} />
          {isCapturing && <ActivityIndicator style={{ marginTop: 12 }} />}
          <Text style={styles.title}>Result</Text>
          <Text selectable>{JSON.stringify(result, null, 2)}</Text>
          <View style={{ marginTop: 12 }}>
            <Button title="Retake" onPress={() => { setPreviewUri(null); setResult(null); }} />
            <Button title="Close" onPress={() => navigation.goBack()} />
          </View>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff' },
  title: { fontSize: 18, fontWeight: '700', marginTop: 12 },
});


