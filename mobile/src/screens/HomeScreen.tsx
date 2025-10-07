import { useState } from 'react';
import { ScrollView, StyleSheet, RefreshControl } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import HeaderCard from '../components/home/HeaderCard';
import QuickActionsRow from '../components/home/QuickActionsRow';
import ImpactSummaryCard from '../components/home/ImpactSummaryCard';
import RecentActivityList from '../components/home/RecentActivityList';
import TipsCarousel from '../components/home/TipsCarousel';
import ImpactBreakdownSheet from '../components/home/ImpactBreakdownSheet';

const mockMetrics = { items: 12, co2eKg: 1.8, waterL: 60, energyKwh: 0.9, eq: { drivingKm: 8, phoneCharges: 224 } };
const mockRecentItems = [
  { id: '1', title: 'Plastic bottle', category: 'Plastic', time: '2h ago' },
  { id: '2', title: 'Aluminum can', category: 'Metal', time: '1d ago' },
  { id: '3', title: 'Paper bag', category: 'Paper', time: '2d ago' },
];
const mockTips = [
  { id: '1', title: 'Rinse before recycling', body: 'Clean containers prevent contamination', icon: 'water-outline' as const },
  { id: '2', title: 'Flatten cardboard', body: 'Saves space in bins and trucks', icon: 'cube-outline' as const },
];

export default function HomeScreen() {
  const { top, bottom } = useSafeAreaInsets();
  const [refreshing, setRefreshing] = useState(false);
  const [sheetVisible, setSheetVisible] = useState(false);

  const onRefresh = async () => {
    setRefreshing(true);
    await new Promise((r) => setTimeout(r, 1000));
    setRefreshing(false);
  };

  return (
    <>
      <ScrollView
        contentContainerStyle={[styles.container, { paddingTop: top + 8, paddingBottom: bottom + 80 }]}
        showsVerticalScrollIndicator={false}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
      >
        <HeaderCard userName="Wizard" points={120} streak={7} onPressAvatar={() => {}} onPressPoints={() => {}} />
        <QuickActionsRow
          actions={[
            { icon: 'scan-outline', label: 'Scan', onPress: () => {} },
            { icon: 'search-outline', label: 'Search', onPress: () => {} },
            { icon: 'map-outline', label: 'Map', onPress: () => {} },
          ]}
        />
        <ImpactSummaryCard period="week" goal={20} metrics={mockMetrics} onPressDetails={() => setSheetVisible(true)} />
        <RecentActivityList items={mockRecentItems} limit={6} onPressItem={() => {}} />
        <TipsCarousel tips={mockTips} onPressTip={() => {}} />
      </ScrollView>
      <ImpactBreakdownSheet visible={sheetVisible} onClose={() => setSheetVisible(false)} metrics={mockMetrics} />
    </>
  );
}

const styles = StyleSheet.create({
  container: { paddingHorizontal: 16, gap: 12 },
});
