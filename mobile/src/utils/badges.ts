import { ActivityEntry } from '../types/profile';
import { User as StoreUser } from '../store/appStore';

export type Badge = {
  id: string;
  name: string;
  icon: string;
  description: string;
  points: number;
  criteria: string;
  locked?: boolean;
};

export function computeBadges(user: StoreUser, entries: ActivityEntry[]): Badge[] {
  const totalItems = entries.length;
  const scans = entries.filter((e) => e.type === 'scan').length;
  const recyclerThreshold = 20;
  const ecoPoints = 200;

  const base: Badge[] = [
    { id: '1', name: 'First Scan', icon: '🌱', description: 'Scanned your first item', points: 10, criteria: 'Scan 1 item' },
    { id: '2', name: '10 Items', icon: '🌿', description: 'Scanned 10 items', points: 50, criteria: 'Scan 10 items' },
    { id: '3', name: 'Streak Master', icon: '🔥', description: '7-day streak', points: 100, criteria: 'Maintain a 7-day streak' },
    { id: '4', name: 'Recycler', icon: '♻️', description: 'Recycled 20 items', points: 150, criteria: 'Recycle 20 items' },
    { id: '5', name: 'Eco Hero', icon: '🌍', description: 'Earned 200 points', points: 200, criteria: 'Reach 200 points' },
  ];

  return base.map((b) => {
    let earned = false;
    if (b.id === '1') earned = scans >= 1;
    if (b.id === '2') earned = totalItems >= 10;
    if (b.id === '3') earned = (user.streakDays ?? 0) >= 7;
    if (b.id === '4') {
      const disposed = entries.filter((e) => e.type === 'disposed').length;
      earned = disposed >= recyclerThreshold;
    }
    if (b.id === '5') earned = (user.points ?? 0) >= ecoPoints;
    return { ...b, locked: !earned } as Badge;
  });
}
