import { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, FlatList } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@react-navigation/native';
import { useAppStore } from '../store/appStore';
import { computeBadges } from '../utils/badges';

export default function BadgesScreen({ navigation }: any) {
  const { colors } = useTheme();
  const [filter, setFilter] = useState<'all' | 'earned' | 'locked'>('all');
  const user = useAppStore((s) => s.user);
  const activity = useAppStore((s) => s.activity);
  const badges = computeBadges(user as any, activity);

  const filtered = badges.filter((b) => {
    if (filter === 'earned') return !b.locked;
    if (filter === 'locked') return b.locked;
    return true;
  });

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { backgroundColor: colors.card, borderBottomColor: colors.border }]}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={24} color={colors.text} />
        </TouchableOpacity>
        <Text style={[styles.headerTitle, { color: colors.text }]}>Badges</Text>
        <View style={{ width: 40 }} />
      </View>

      <View style={[styles.filterRow, { backgroundColor: colors.card }]}>
        {(['all', 'earned', 'locked'] as const).map((f) => (
          <TouchableOpacity key={f} onPress={() => setFilter(f)} style={[styles.filterChip, { backgroundColor: colors.border }, filter === f && { backgroundColor: '#e0f2f1' }]}>
            <Text style={[styles.filterText, { color: colors.text + '99' }, filter === f && { color: '#00695c' }]}>{f.charAt(0).toUpperCase() + f.slice(1)}</Text>
          </TouchableOpacity>
        ))}
      </View>

      <FlatList
        data={filtered}
        numColumns={2}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.grid}
        ListEmptyComponent={<Text style={{ textAlign: 'center', color: colors.text + '99', marginTop: 20 }}>No badges yet. Scan to start earning!</Text>}
        renderItem={({ item }) => (
          <TouchableOpacity style={[styles.badgeCard, { backgroundColor: colors.card }, item.locked && styles.badgeCardLocked]}>
            <View style={[styles.badgeIcon, { backgroundColor: '#e0f2f1' }]}>
              <Text style={styles.badgeIconText}>{item.icon}</Text>
              {item.locked && <Ionicons name="lock-closed" size={16} color="#999" style={styles.lock} />}
            </View>
            <Text style={[styles.badgeName, { color: colors.text }]}>{item.name}</Text>
            <Text style={[styles.badgeDesc, { color: colors.text + '99' }]}>{item.description}</Text>
            <Text style={styles.badgePoints}>{item.points} pts</Text>
          </TouchableOpacity>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 16, paddingVertical: 12, borderBottomWidth: 1 },
  backBtn: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
  headerTitle: { fontSize: 18, fontWeight: '700' },
  filterRow: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, paddingVertical: 12 },
  filterChip: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 999 },
  filterChipActive: { backgroundColor: '#e0f2f1' },
  filterText: { fontSize: 13, fontWeight: '600' },
  filterTextActive: { color: '#00695c' },
  grid: { padding: 12, gap: 12 },
  badgeCard: { flex: 1, margin: 4, borderRadius: 12, padding: 16, alignItems: 'center', shadowColor: '#000', shadowOpacity: 0.06, shadowRadius: 8, elevation: 2 },
  badgeCardLocked: { opacity: 0.6 },
  badgeIcon: { width: 72, height: 72, borderRadius: 36, alignItems: 'center', justifyContent: 'center', marginBottom: 10, position: 'relative' },
  badgeIconText: { fontSize: 32 },
  lock: { position: 'absolute', bottom: 4, right: 4 },
  badgeName: { fontSize: 15, fontWeight: '700', marginBottom: 4, textAlign: 'center' },
  badgeDesc: { fontSize: 12, marginBottom: 6, textAlign: 'center' },
  badgePoints: { fontSize: 13, fontWeight: '600', color: '#10b981' },
  badgeEarned: { fontSize: 11, marginTop: 4 },
});

