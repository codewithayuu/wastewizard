import { StatusBar } from 'expo-status-bar';
import { useEffect, useRef, useState } from 'react';
import { ActivityIndicator, Button, Image, StyleSheet, Text, View, Alert, ScrollView, Modal, TextInput, TouchableOpacity } from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import * as ImageManipulator from 'expo-image-manipulator';
import * as FileSystem from 'expo-file-system';
import { API_BASE_URL, getGuidance, infer, type Detection } from './src/api';
import { getStoredApiBaseUrl, setStoredApiBaseUrl } from './src/storage';

export default function App() {
  const cameraRef = useRef<CameraView>(null);
  const [permission, requestPermission] = useCameraPermissions();
  const [isCapturing, setIsCapturing] = useState(false);
  const [previewUri, setPreviewUri] = useState<string | null>(null);
  const [detections, setDetections] = useState<Detection[] | null>(null);
  const [guidance, setGuidance] = useState<any | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [settingsVisible, setSettingsVisible] = useState(false);
  const [apiBaseUrl, setApiBaseUrl] = useState<string>(API_BASE_URL);

  useEffect(() => {
    if (!permission) {
      requestPermission();
    }
    (async () => {
      const stored = await getStoredApiBaseUrl();
      if (stored) setApiBaseUrl(stored);
    })();
  }, [permission, requestPermission]);

  const reset = () => {
    setPreviewUri(null);
    setDetections(null);
    setGuidance(null);
    setError(null);
  };

  const captureAndInfer = async () => {
    if (!permission?.granted) {
      const granted = await requestPermission();
      if (!granted.granted) {
        Alert.alert('Permission required', 'Camera permission is needed to take a photo.');
        return;
      }
    }
    try {
      setIsCapturing(true);
      setError(null);
      // Capture photo
      const cam = cameraRef.current;
      if (!cam) throw new Error('Camera not ready');
      const photo = await cam.takePictureAsync({ skipProcessing: true, quality: 0.9, base64: true });

      // Resize/compress for faster upload
      const resized = await ImageManipulator.manipulateAsync(
        photo.uri,
        [{ resize: { width: 768 } }],
        { compress: 0.8, format: ImageManipulator.SaveFormat.JPEG }
      );
      setPreviewUri(resized.uri);

      // Prefer camera base64; fallback to reading file
      let base64 = (photo as any).base64 as string | undefined;
      if (!base64) {
        base64 = await FileSystem.readAsStringAsync(resized.uri, { encoding: FileSystem.EncodingType.Base64 });
      }

      // Call backend
      const inferRes = await infer(base64);
      setDetections(inferRes.detections);

      // Fetch guidance for the top detection
      if (inferRes.detections.length > 0) {
        const top = inferRes.detections[0];
        try {
          const g = await getGuidance(top.label);
          setGuidance(g.guidance);
        } catch (e: any) {
          setGuidance(null);
        }
      } else {
        setGuidance(null);
      }
    } catch (e: any) {
      setError(e?.message || 'Capture failed');
    } finally {
      setIsCapturing(false);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Waste Wizard</Text>
        <TouchableOpacity onPress={() => setSettingsVisible(true)} style={styles.settingsBtn}>
          <Text style={styles.settingsText}>Settings</Text>
        </TouchableOpacity>
      </View>
      {!previewUri ? (
        <>
          {permission?.granted ? (
            <CameraView ref={cameraRef} style={styles.camera} />
          ) : (
            <View style={styles.center}>
              <Text>Camera permission required</Text>
              <Button title="Grant permission" onPress={() => requestPermission()} />
            </View>
          )}
          <View style={styles.actions}>
            <Button title={isCapturing ? 'Capturing…' : 'Capture'} onPress={captureAndInfer} disabled={isCapturing} />
          </View>
        </>
      ) : (
        <ScrollView contentContainerStyle={styles.resultContainer}>
          <Image source={{ uri: previewUri }} style={styles.preview} />
          {isCapturing && <ActivityIndicator style={{ marginTop: 12 }} />}
          {error && <Text style={styles.error}>{error}</Text>}
          {detections && (
            <View style={styles.card}>
              <Text style={styles.heading}>Detections</Text>
              {detections.length === 0 ? (
                <Text>None above threshold.</Text>
              ) : (
                detections.map((d, idx) => (
                  <Text key={idx}>{`${d.label} (${(d.confidence * 100).toFixed(1)}%)`}</Text>
                ))
              )}
            </View>
          )}
          {guidance && (
            <View style={styles.card}>
              <Text style={styles.heading}>Guidance</Text>
              {typeof guidance === 'string' ? (
                <Text>{guidance}</Text>
              ) : (
                <>
                  {guidance.instructions && <Text>{guidance.instructions}</Text>}
                  {Array.isArray(guidance.preparation) && guidance.preparation.length > 0 && (
                    <Text>{`Preparation: ${guidance.preparation.join(', ')}`}</Text>
                  )}
                  {guidance.notes && <Text>{`Notes: ${guidance.notes}`}</Text>}
                </>
              )}
            </View>
          )}
          <View style={styles.actions}>
            <Button title="Retake" onPress={reset} />
          </View>
        </ScrollView>
      )}
      <StatusBar style="auto" />

      <Modal visible={settingsVisible} animationType="slide" transparent>
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard}>
            <Text style={styles.heading}>Settings</Text>
            <Text style={{ marginBottom: 4 }}>API Base URL</Text>
            <TextInput
              value={apiBaseUrl}
              onChangeText={setApiBaseUrl}
              placeholder="http://<IP>:8000"
              autoCapitalize="none"
              style={styles.input}
            />
            <View style={{ flexDirection: 'row', justifyContent: 'flex-end', marginTop: 12 }}>
              <Button title="Cancel" onPress={() => setSettingsVisible(false)} />
              <View style={{ width: 8 }} />
              <Button
                title="Save"
                onPress={async () => {
                  await setStoredApiBaseUrl(apiBaseUrl);
                  setSettingsVisible(false);
                  Alert.alert('Saved', 'API base URL updated.');
                }}
              />
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  camera: { flex: 1 },
  actions: { padding: 12 },
  resultContainer: { padding: 12 },
  preview: { width: '100%', height: 300, borderRadius: 8, backgroundColor: '#eee' },
  card: { marginTop: 12, padding: 12, borderRadius: 8, backgroundColor: '#f6f6f6' },
  heading: { fontWeight: '600', marginBottom: 8 },
  error: { color: 'red', marginTop: 8 },
  header: { paddingTop: 12, paddingHorizontal: 12, paddingBottom: 8, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  title: { fontSize: 18, fontWeight: '700' },
  settingsBtn: { paddingHorizontal: 10, paddingVertical: 6, backgroundColor: '#efefef', borderRadius: 6 },
  settingsText: { fontWeight: '600' },
  modalBackdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.35)', alignItems: 'center', justifyContent: 'center', padding: 16 },
  modalCard: { width: '100%', maxWidth: 420, backgroundColor: 'white', borderRadius: 12, padding: 16 },
  input: { borderWidth: 1, borderColor: '#ddd', borderRadius: 8, paddingHorizontal: 10, paddingVertical: 8 },
});
