import { useState } from 'react';
import { ScrollView, StyleSheet, RefreshControl } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import HeaderCard from '../components/home/HeaderCard';
import QuickActionsRow from '../components/home/QuickActionsRow';
import ImpactSummaryCard from '../components/home/ImpactSummaryCard';
import RecentActivityList from '../components/home/RecentActivityList';
import TipsCarousel from '../components/home/TipsCarousel';
import ImpactBreakdownSheet from '../components/home/ImpactBreakdownSheet';
import LeagueCard from '../components/home/LeagueCard';
import SegregationBars from '../components/home/SegregationBars';
import WeeklyGoalCard from '../components/home/WeeklyGoalCard';
import { computeWeeklyImpact } from '../utils/impact';
import { useAppStore } from '../store/appStore';

const tipsData = [
  { id: '1', title: 'Rinse before recycling', body: 'Clean containers prevent contamination', icon: 'water-outline' as const },
  { id: '2', title: 'Flatten cardboard', body: 'Saves space in bins and trucks', icon: 'cube-outline' as const },
];

export default function HomeScreen() {
  const { top, bottom } = useSafeAreaInsets();
  const [refreshing, setRefreshing] = useState(false);
  const [sheetVisible, setSheetVisible] = useState(false);
  const navigation: any = useNavigation();
  const user = useAppStore((s) => s.user);
  const tipsOn = useAppStore((s) => s.settings.notifications.tips);
  const activity = useAppStore((s) => s.activity);

  const onRefresh = async () => {
    setRefreshing(true);
    await new Promise((r) => setTimeout(r, 1000));
    setRefreshing(false);
  };

  // derive dynamic metrics and recent activity
  const metrics = computeWeeklyImpact(activity);
  const recent = activity.slice(0, 6).map((e) => ({ id: e.id, title: e.title, category: e.material.charAt(0).toUpperCase() + e.material.slice(1), time: e.when }));

  return (
    <>
      <ScrollView
        contentContainerStyle={[styles.container, { paddingTop: top + 8, paddingBottom: bottom + 80 }]}
        showsVerticalScrollIndicator={false}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
      >
        <HeaderCard userName={user.name || 'Guest'} points={user.points} streak={user.streakDays} onPressAvatar={() => {}} onPressPoints={() => {}} />
        <QuickActionsRow
          actions={[
            { icon: 'scan-outline', label: 'Scan', onPress: () => navigation.navigate('ScanModal') },
            { icon: 'search-outline', label: 'Search', onPress: () => {} },
          ]}
        />
        <ImpactSummaryCard period="week" goal={20} metrics={metrics} onPressDetails={() => setSheetVisible(true)} />
        <RecentActivityList items={recent} limit={6} onPressItem={() => {}} />
        {tipsOn ? <TipsCarousel tips={tipsData} onPressTip={() => {}} /> : null}
        <LeagueCard />
        <SegregationBars />
        <WeeklyGoalCard />
      </ScrollView>
      <ImpactBreakdownSheet visible={sheetVisible} onClose={() => setSheetVisible(false)} metrics={metrics} />
    </>
  );
}

const styles = StyleSheet.create({
  container: { paddingHorizontal: 16, gap: 12 },
});
