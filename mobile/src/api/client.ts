import { ActivityEntry } from '../store/appStore';
import { getStoredApiBaseUrl } from '../storage';
import { Platform } from 'react-native';

async function baseUrl(): Promise<string> {
  const stored = await getStoredApiBaseUrl();
  return stored || 'http://localhost:4000';
}

async function request(path: string, init?: RequestInit) {
  const url = `${await baseUrl()}${path}`;
  const res = await fetch(url, { headers: { 'Content-Type': 'application/json' }, ...init });
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.status === 204 ? null : res.json();
}

export async function getUser() {
  return request('/api/user');
}
export async function updateUser(partial: any) {
  return request('/api/user', { method: 'PUT', body: JSON.stringify(partial) });
}
export async function getSettings() {
  return request('/api/settings');
}
export async function updateSettings(partial: any) {
  return request('/api/settings', { method: 'PUT', body: JSON.stringify(partial) });
}
export async function getActivity() {
  return request('/api/activity');
}
export async function postActivity(e: ActivityEntry) {
  return request('/api/activity', { method: 'POST', body: JSON.stringify(e) });
}
export async function getImpact() {
  return request('/api/impact');
}
export async function resetAll() {
  return request('/api/reset', { method: 'POST' });
}

// FastAPI predictor (separate service)
function defaultPredictBase() {
  if (Platform.OS === 'android') return 'http://10.0.2.2:8000';
  return 'http://127.0.0.1:8000';
}

export async function predictGarbage(imageUri: string): Promise<{ class: string; confidence: number }> {
  const override = await getStoredApiBaseUrl();
  const base = override || defaultPredictBase();
  const url = `${base}/predict`;
  const form = new FormData();
  form.append('file', { uri: imageUri, name: 'photo.jpg', type: 'image/jpeg' } as any);
  const res = await fetch(url, { method: 'POST', body: form as any });
  if (!res.ok) throw new Error(`Predict failed ${res.status}`);
  return res.json();
}
