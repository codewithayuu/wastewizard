import { useRef, useState } from 'react';
import { Alert, Linking, Platform, ScrollView, Share, StyleSheet, View } from 'react-native';
import ProfileHeader from '../components/profile/ProfileHeader';
import ImpactRewardsCard from '../components/profile/ImpactRewardsCard';
import ActivityCard from '../components/profile/ActivityCard';
import PreferencesCard from '../components/profile/PreferencesCard';
import DataPrivacyCard from '../components/profile/DataPrivacyCard';
import HelpLegalCard from '../components/profile/HelpLegalCard';
import SignOutRow from '../components/profile/SignOutRow';
import TextPromptModal from '../components/common/TextPromptModal';
import { User } from '../types/profile';
import { useAppStore, useLeague } from '../store/appStore';
import SegregationBars from '../components/home/SegregationBars';
import * as FileSystem from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import { computeBadges } from '../utils/badges';

//

export default function ProfileScreen({ navigation }: any) {
  const user = useAppStore((s) => s.user);
  const activity = useAppStore((s) => s.activity);
  const settings = useAppStore((s) => s.settings);
  const { league } = useLeague();
  const signOut = useAppStore((s) => s.signOut);
  const updateSettings = useAppStore((s) => s.updateSettings);
  const setUser = useAppStore((s) => s.setUser);
  const resetApp = useAppStore((s) => s.resetApp);

  const scrollRef = useRef<ScrollView>(null);
  const [editNameOpen, setEditNameOpen] = useState(false);

  // Compose a profile User that includes preferences from settings to avoid undefined access
  const profileUser: User = {
    id: user.id ?? 'guest',
    name: user.name ?? 'Guest',
    email: user.email,
    photoURL: user.photoURL,
    isGuest: user.isGuest,
    points: user.points,
    streakDays: user.streakDays,
    level: league.title as any,
    preferences: {
      language: settings.language,
      theme: settings.theme,
      units: settings.units,
      notifications: settings.notifications,
      accessibility: settings.accessibility,
    },
  };

  const handleShare = async () => {
    await Share.share({ message: `I'm in ${league.title} with ${user.points} pts on Waste Wizard.` });
  };

  const handleManageAccount = () => {
    Alert.alert('Account', user.email || 'Profile', [
      { text: 'Edit name', onPress: () => setEditNameOpen(true) },
      { text: 'Sign out', style: 'destructive', onPress: signOut },
      { text: 'Close', style: 'cancel' },
    ]);
  };

  const handleExport = async () => {
    try {
      const rows = [
        ['id', 'type', 'title', 'material', 'when', 'points'],
        ...activity.map((e) => [e.id, e.type, e.title, e.material, e.when, e.points ?? ''])
      ];
      const csv = rows.map((r) => r.map((v) => `"${String(v).replace(/"/g, '""')}"`).join(',')).join('\n');
      const filename = `activity-${Date.now()}.csv`;
      const docDir = (FileSystem as any).documentDirectory as string | undefined;
      const cacheDir = (FileSystem as any).cacheDirectory as string | undefined;
      const baseDir = docDir || cacheDir || '';
      if (!baseDir) {
        Alert.alert('Export unavailable', 'No writable directory found.');
        return;
      }
      const uri = `${baseDir}${filename}`;
      await FileSystem.writeAsStringAsync(uri, csv, { encoding: 'utf8' as any });
      if (await Sharing.isAvailableAsync()) {
        await Sharing.shareAsync(uri, { mimeType: 'text/csv', dialogTitle: 'Export activity' });
      } else {
        // Fallback open
        if (Platform.OS === 'android') {
          const contentUri = await (FileSystem as any).getContentUriAsync(uri);
          await Linking.openURL(contentUri);
        } else {
          await Linking.openURL(uri);
        }
      }
      Alert.alert('Export complete', `Saved to ${uri}`);
    } catch (e: any) {
      Alert.alert('Export failed', e?.message || String(e));
    }
  };

  const handleClearCache = async () => {
    try {
      const cacheDir = (FileSystem as any).cacheDirectory as string | undefined;
      if (cacheDir) await FileSystem.deleteAsync(cacheDir, { idempotent: true });
      Alert.alert('Cache cleared');
    } catch (e: any) {
      Alert.alert('Failed to clear cache', e?.message || String(e));
    }
  };

  const handleDeleteAccount = async () => {
    Alert.alert('Delete account', 'This cannot be undone. Continue?', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: () => { resetApp(); } },
    ]);
  };

  const openUrl = (u: string) => Linking.openURL(u);
  const badges = computeBadges(user as any, activity);

  const modules = [
    <ProfileHeader
      key="header"
      user={profileUser}
      onShare={handleShare}
      onSettings={() => scrollRef.current?.scrollTo({ y: 600, animated: true })}
      onEditName={() => setEditNameOpen(true)}
    />,
    <ImpactRewardsCard key="impact" user={profileUser} badges={badges} onViewBadges={() => navigation.navigate('Badges')} />,
    <ActivityCard key="activity" entries={activity.slice(0, 4)} onViewAll={() => navigation.navigate('Activity')} />,
    <PreferencesCard key="prefs" user={profileUser} onUpdate={(prefs) => updateSettings(prefs)} />,
    <DataPrivacyCard
      key="privacy"
      isGuest={user.isGuest}
      onManageAccount={handleManageAccount}
      onExport={handleExport}
      onDelete={handleDeleteAccount}
    />,
    <HelpLegalCard
      key="help"
      onFAQ={() => openUrl('https://wastewizard.app/help')}
      onContact={() => Linking.openURL('mailto:codewithayuu@gmail.com?subject=Support')}
      onReport={() => Linking.openURL('mailto:codewithayuu@gmail.com?subject=Bug%20report')}
      onTerms={() => openUrl('https://wastewizard.app/terms')}
      onPrivacy={() => openUrl('https://wastewizard.app/privacy')}
    />,
    !user.isGuest && <SignOutRow key="signout" onSignOut={signOut} />,
  ].filter(Boolean);

  return (
    <ScrollView ref={scrollRef} contentContainerStyle={styles.container}>
      {modules.map((m, i) => <View key={i}>{m}</View>)}
      <TextPromptModal
        visible={editNameOpen}
        title="Your name"
        placeholder="Enter your name"
        initialValue={user.name || ''}
        onCancel={() => setEditNameOpen(false)}
        onSubmit={(val) => { setUser({ name: val || 'Guest', isGuest: false }); setEditNameOpen(false); }}
      />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { padding: 16, gap: 12, paddingBottom: 100 },
});
