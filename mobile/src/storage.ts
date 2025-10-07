import AsyncStorage from '@react-native-async-storage/async-storage';

const KEY_API_BASE_URL = 'apiBaseUrl';

let cachedApiBaseUrl: string | null = null;

export async function getStoredApiBaseUrl(): Promise<string | null> {
  try {
    if (cachedApiBaseUrl !== null) return cachedApiBaseUrl;
    const v = await AsyncStorage.getItem(KEY_API_BASE_URL);
    cachedApiBaseUrl = v;
    return v;
  } catch {
    return null;
  }
}

export async function setStoredApiBaseUrl(url: string): Promise<void> {
  cachedApiBaseUrl = url;
  await AsyncStorage.setItem(KEY_API_BASE_URL, url);
}


