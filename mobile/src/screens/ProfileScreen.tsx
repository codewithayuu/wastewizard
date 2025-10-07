import { useState } from 'react';
import { ScrollView, StyleSheet } from 'react-native';
import ProfileHeader from '../components/profile/ProfileHeader';
import ImpactRewardsCard from '../components/profile/ImpactRewardsCard';
import ActivityCard from '../components/profile/ActivityCard';
import PreferencesCard from '../components/profile/PreferencesCard';
import DataPrivacyCard from '../components/profile/DataPrivacyCard';
import HelpLegalCard from '../components/profile/HelpLegalCard';
import SignOutRow from '../components/profile/SignOutRow';
import { User, Badge, ActivityEntry } from '../types/profile';

const mockUser: User = {
  id: '1',
  name: 'Ayush',
  email: 'ayush@example.com',
  photoURL: undefined,
  isGuest: true,
  points: 1240,
  streakDays: 6,
  level: 'Sprout',
  preferences: {
    language: 'English',
    theme: 'system',
    units: 'metric',
    notifications: { reminders: true, tips: true, achievements: true },
    accessibility: { largerText: false, reducedMotion: false, haptics: true },
  },
};

const mockBadges: Badge[] = [
  { id: '1', name: 'First Scan', icon: '🌱', description: 'Scanned your first item', points: 10, earnedAt: '2024-01-01', criteria: 'Scan 1 item' },
  { id: '2', name: '10 Items', icon: '🌿', description: 'Scanned 10 items', points: 50, earnedAt: '2024-01-10', criteria: 'Scan 10 items' },
  { id: '3', name: 'Streak Master', icon: '🔥', description: '7-day streak', points: 100, criteria: 'Maintain a 7-day streak', locked: true },
  { id: '4', name: 'Recycler', icon: '♻️', description: 'Recycled 20 items', points: 150, criteria: 'Recycle 20 items', locked: true },
  { id: '5', name: 'Eco Hero', icon: '🌍', description: 'Avoided 5 kg CO₂e', points: 200, criteria: 'Avoid 5 kg CO₂e', locked: true },
];

const mockActivity: ActivityEntry[] = [
  { id: '1', type: 'scan', title: 'Plastic bottle', material: 'plastic', when: '2h ago', points: 10, thumb: undefined },
  { id: '2', type: 'disposed', title: 'Glass jar', material: 'glass', when: '5h ago', points: 15, thumb: undefined },
  { id: '3', type: 'bookmark', title: 'Aluminum can', material: 'metal', when: '1d ago', points: 10, thumb: undefined },
];

export default function ProfileScreen({ navigation }: any) {
  const [user, setUser] = useState(mockUser);

  const modules = [
    <ProfileHeader key="header" user={user} onShare={() => {}} onSettings={() => {}} onGoogleSignIn={() => setUser({ ...user, isGuest: false })} />,
    <ImpactRewardsCard key="impact" user={user} badges={mockBadges} onViewBadges={() => {}} />,
    <ActivityCard key="activity" entries={mockActivity} onViewAll={() => {}} />,
    <PreferencesCard key="prefs" user={user} onUpdate={(prefs) => setUser({ ...user, preferences: prefs })} />,
    <DataPrivacyCard key="privacy" isGuest={user.isGuest} />,
    <HelpLegalCard key="help" />,
    !user.isGuest && <SignOutRow key="signout" onSignOut={() => setUser({ ...user, isGuest: true })} />,
  ].filter(Boolean);

  return <ScrollView contentContainerStyle={styles.container}>{modules}</ScrollView>;
}

const styles = StyleSheet.create({
  container: { padding: 16, gap: 12, paddingBottom: 100 },
});
