import AsyncStorage from '@react-native-async-storage/async-storage';
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

export type Material = 'plastic' | 'paper' | 'glass' | 'metal' | 'organic' | 'e-waste' | 'hazardous';

export type ActivityEntry = {
  id: string;
  type: 'scan' | 'disposed' | 'bookmark';
  title: string;
  material: Material;
  when: string;
  points?: number;
  thumb?: string;
};

export type League = { title: string; char: string; color: string; min: number };

const leagues: League[] = [
  { min: 0, title: 'Seedling', char: '🌱', color: '#10B981' },
  { min: 100, title: 'Sprout', char: '🌿', color: '#34D399' },
  { min: 300, title: 'Sapling', char: '🌳', color: '#059669' },
  { min: 600, title: 'Grove', char: '🌲', color: '#047857' },
  { min: 1000, title: 'Guardian', char: '🛡️', color: '#065F46' },
  { min: 1500, title: 'Wizard', char: '🧙‍♂️', color: '#0F766E' },
];

export const getLeague = (points: number): League => {
  return leagues.reduce((acc, l) => (points >= l.min ? l : acc), leagues[0]);
};

export const getNextLeague = (points: number): League | undefined => {
  return leagues.find((l) => l.min > points);
};

export type User = {
  id?: string;
  name?: string;
  email?: string;
  photoURL?: string;
  isGuest: boolean;
  points: number;
  streakDays: number;
  segregatedCounts: Record<Material, number>;
};

export type Settings = {
  theme: 'system' | 'light' | 'dark';
  language: string;
  units: 'metric' | 'imperial';
  notifications: { reminders: boolean; tips: boolean; achievements: boolean };
  accessibility: { largerText: boolean; reducedMotion: boolean; haptics: boolean };
  weeklyGoal: number;
};

export type AppState = {
  user: User;
  activity: ActivityEntry[];
  settings: Settings;
  addActivity: (e: ActivityEntry) => void;
  addPoints: (n: number, material?: Material) => void;
  updateSettings: (s: Partial<Settings>) => void;
  setWeeklyGoal: (goal: number) => void;
  signInGoogle: (profile: Partial<User>) => void;
  signOut: () => void;
};

export const useAppStore = create<AppState>()(
  persist(
    (set) => ({
      user: {
        isGuest: true,
        points: 0,
        streakDays: 0,
        segregatedCounts: { plastic: 0, paper: 0, glass: 0, metal: 0, organic: 0, 'e-waste': 0, hazardous: 0 },
      },
      activity: [],
      settings: {
        theme: 'system',
        language: 'English',
        units: 'metric',
        notifications: { reminders: true, tips: true, achievements: true },
        accessibility: { largerText: false, reducedMotion: false, haptics: true },
        weeklyGoal: 20,
      },
      addActivity: (e) => set((s) => ({ activity: [e, ...s.activity] })),
      addPoints: (n, material) =>
        set((s) => {
          const u = { ...s.user, points: s.user.points + n };
          if (material) u.segregatedCounts = { ...u.segregatedCounts, [material]: (u.segregatedCounts[material] ?? 0) + 1 };
          return { user: u };
        }),
      updateSettings: (partial) => set((s) => ({ settings: { ...s.settings, ...partial } })),
      setWeeklyGoal: (goal) => set((s) => ({ settings: { ...s.settings, weeklyGoal: goal } })),
      signInGoogle: (profile) => set((s) => ({ user: { ...s.user, ...profile, isGuest: false } })),
      signOut: () =>
        set({
          user: {
            isGuest: true,
            points: 0,
            streakDays: 0,
            segregatedCounts: { plastic: 0, paper: 0, glass: 0, metal: 0, organic: 0, 'e-waste': 0, hazardous: 0 },
          },
        }),
    }),
    { name: 'waste-wizard-store', storage: createJSONStorage(() => AsyncStorage) }
  )
);

export const useLeague = () => {
  const points = useAppStore((s) => s.user.points);
  const league = getLeague(points);
  const next = getNextLeague(points);
  const toNext = next ? next.min - points : 0;
  return { league, next, toNext };
};

