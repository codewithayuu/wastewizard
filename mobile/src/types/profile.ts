export type User = {
  id: string;
  name: string;
  email?: string;
  photoURL?: string;
  isGuest: boolean;
  points: number;
  streakDays: number;
  level: 'Seedling' | 'Sprout' | 'Grove' | 'Forest';
  preferences: {
    language: string;
    theme: 'system' | 'light' | 'dark';
    units: 'metric' | 'imperial';
    notifications: { reminders: boolean; tips: boolean; achievements: boolean };
    accessibility: { largerText: boolean; reducedMotion: boolean; haptics: boolean };
    media?: { savePhotos: boolean; cellularUploads: boolean };
  };
};

export type Badge = {
  id: string;
  name: string;
  icon: string;
  description: string;
  points: number;
  earnedAt?: string;
  criteria: string;
  locked?: boolean;
};

export type ActivityEntry = {
  id: string;
  type: 'scan' | 'disposed' | 'bookmark';
  title: string;
  material: 'plastic' | 'paper' | 'glass' | 'metal' | 'organic' | 'e-waste' | 'hazardous';
  when: string;
  points?: number;
  thumb?: string;
};

export type ImpactStats = {
  days: { date: string; items: number; co2eKg?: number }[];
};

