import { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, FlatList } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Badge } from '../types/profile';

const mockBadges: Badge[] = [
  { id: '1', name: 'First Scan', icon: '🌱', description: 'Scanned your first item', points: 10, earnedAt: '2024-01-01', criteria: 'Scan 1 item' },
  { id: '2', name: '10 Items', icon: '🌿', description: 'Scanned 10 items', points: 50, earnedAt: '2024-01-10', criteria: 'Scan 10 items' },
  { id: '3', name: 'Streak Master', icon: '🔥', description: '7-day streak', points: 100, criteria: 'Maintain a 7-day streak', locked: true },
  { id: '4', name: 'Recycler', icon: '♻️', description: 'Recycled 20 items', points: 150, criteria: 'Recycle 20 items', locked: true },
  { id: '5', name: 'Eco Hero', icon: '🌍', description: 'Avoided 5 kg CO₂e', points: 200, criteria: 'Avoid 5 kg CO₂e', locked: true },
  { id: '6', name: 'Compost King', icon: '🍂', description: 'Composted 30 items', points: 250, criteria: 'Compost 30 items', locked: true },
];

export default function BadgesScreen({ navigation }: any) {
  const [filter, setFilter] = useState<'all' | 'earned' | 'locked'>('all');

  const filtered = mockBadges.filter((b) => {
    if (filter === 'earned') return !b.locked;
    if (filter === 'locked') return b.locked;
    return true;
  });

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={24} color="#333" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Badges</Text>
        <View style={{ width: 40 }} />
      </View>

      <View style={styles.filterRow}>
        {(['all', 'earned', 'locked'] as const).map((f) => (
          <TouchableOpacity key={f} onPress={() => setFilter(f)} style={[styles.filterChip, filter === f && styles.filterChipActive]}>
            <Text style={[styles.filterText, filter === f && styles.filterTextActive]}>{f.charAt(0).toUpperCase() + f.slice(1)}</Text>
          </TouchableOpacity>
        ))}
      </View>

      <FlatList
        data={filtered}
        numColumns={2}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.grid}
        renderItem={({ item }) => (
          <TouchableOpacity style={[styles.badgeCard, item.locked && styles.badgeCardLocked]}>
            <View style={styles.badgeIcon}>
              <Text style={styles.badgeIconText}>{item.icon}</Text>
              {item.locked && <Ionicons name="lock-closed" size={16} color="#999" style={styles.lock} />}
            </View>
            <Text style={styles.badgeName}>{item.name}</Text>
            <Text style={styles.badgeDesc}>{item.description}</Text>
            <Text style={styles.badgePoints}>{item.points} pts</Text>
            {item.earnedAt && <Text style={styles.badgeEarned}>Earned {item.earnedAt}</Text>}
          </TouchableOpacity>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f9fafb' },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 16, paddingVertical: 12, backgroundColor: '#fff', borderBottomWidth: 1, borderBottomColor: '#e5e7eb' },
  backBtn: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
  headerTitle: { fontSize: 18, fontWeight: '700' },
  filterRow: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, paddingVertical: 12, backgroundColor: '#fff' },
  filterChip: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 999, backgroundColor: '#f3f4f6' },
  filterChipActive: { backgroundColor: '#e0f2f1' },
  filterText: { fontSize: 13, fontWeight: '600', color: '#666' },
  filterTextActive: { color: '#00695c' },
  grid: { padding: 12, gap: 12 },
  badgeCard: { flex: 1, margin: 4, backgroundColor: '#fff', borderRadius: 12, padding: 16, alignItems: 'center', shadowColor: '#000', shadowOpacity: 0.06, shadowRadius: 8, elevation: 2 },
  badgeCardLocked: { opacity: 0.6 },
  badgeIcon: { width: 72, height: 72, borderRadius: 36, backgroundColor: '#e0f2f1', alignItems: 'center', justifyContent: 'center', marginBottom: 10, position: 'relative' },
  badgeIconText: { fontSize: 32 },
  lock: { position: 'absolute', bottom: 4, right: 4 },
  badgeName: { fontSize: 15, fontWeight: '700', marginBottom: 4, textAlign: 'center' },
  badgeDesc: { fontSize: 12, color: '#666', marginBottom: 6, textAlign: 'center' },
  badgePoints: { fontSize: 13, fontWeight: '600', color: '#10b981' },
  badgeEarned: { fontSize: 11, color: '#999', marginTop: 4 },
});

