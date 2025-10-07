import { Platform } from 'react-native';
import { getStoredApiBaseUrl } from './storage';

const fallbackBaseUrl = Platform.OS === 'android' ? 'http://10.0.2.2:8000' : 'http://localhost:8000';
const configuredBaseUrl = process.env.EXPO_PUBLIC_API_BASE_URL;

export const API_BASE_URL = configuredBaseUrl && configuredBaseUrl.length > 0 ? configuredBaseUrl : fallbackBaseUrl;

export type Detection = {
  label: string;
  confidence: number;
  bbox?: number[];
};

export type InferResponse = {
  detections: Detection[];
  abstain: null | { reason: string; threshold: number };
};

export async function infer(imageBase64: string): Promise<InferResponse> {
  const override = await getStoredApiBaseUrl();
  const base = override && override.length > 0 ? override : API_BASE_URL;
  const res = await fetch(`${base}/infer`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ image_base64: imageBase64 }),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`Inference failed (${res.status}): ${text}`);
  }
  return res.json();
}

export async function getGuidance(category: string): Promise<{ category: string; guidance: any }> {
  const override = await getStoredApiBaseUrl();
  const base = override && override.length > 0 ? override : API_BASE_URL;
  const params = new URLSearchParams({ category });
  const res = await fetch(`${base}/rules?${params.toString()}`);
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`Rules lookup failed (${res.status}): ${text}`);
  }
  return res.json();
}


