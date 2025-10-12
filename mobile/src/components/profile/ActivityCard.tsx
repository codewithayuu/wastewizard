import { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Image } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@react-navigation/native';
import { ActivityEntry } from '../../types/profile';

type Props = { entries: ActivityEntry[]; onViewAll?: () => void };

export default function ActivityCard({ entries, onViewAll }: Props) {
  const { colors } = useTheme();
  const [segment, setSegment] = useState<'scans' | 'disposed' | 'bookmarks'>('scans');
  const [filter, setFilter] = useState('All');

  const segments = ['Scans', 'Disposed', 'Bookmarks'];
  const filters = ['All', 'Plastic', 'Paper', 'Glass', 'Metal', 'Organic', 'E‑waste', 'Hazardous'];

  return (
    <View style={[styles.card, { backgroundColor: colors.card }]}>
      <Text style={[styles.title, { color: colors.text }]}>Activity</Text>

      <View style={[styles.segmentedControl, { backgroundColor: colors.border }] }>
        {segments.map((s) => {
          const key = s.toLowerCase() as 'scans' | 'disposed' | 'bookmarks';
          return (
            <TouchableOpacity key={s} onPress={() => setSegment(key)} style={[styles.segmentBtn, segment === key && { backgroundColor: colors.card }]}>
              <Text style={[styles.segmentText, { color: colors.text + '99' }, segment === key && { color: colors.text }]}>{s}</Text>
            </TouchableOpacity>
          );
        })}
      </View>

      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.filtersRow}>
        {filters.map((f) => (
          <TouchableOpacity key={f} onPress={() => setFilter(f)} style={[styles.filterChip, { backgroundColor: colors.border }, filter === f && { backgroundColor: '#e0f2f1' }]}>
            <Text style={[styles.filterText, { color: colors.text + '99' }, filter === f && { color: '#00695c' }]}>{f}</Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      {entries.length === 0 ? (
        <View style={styles.empty}>
          <Ionicons name="leaf-outline" size={48} color={colors.text + '55'} />
          <Text style={[styles.emptyText, { color: colors.text + '99' }]}>No activity yet—try Scan to add your first item.</Text>
        </View>
      ) : (
        <View style={styles.list}>
          {entries.slice(0, 4).map((e) => (
            <View key={e.id} style={styles.row}>
              <View style={styles.thumb}>
                {e.thumb ? <Image source={{ uri: e.thumb }} style={styles.thumbImg} /> : <Ionicons name="cube-outline" size={20} color={colors.text + '66'} />}
              </View>
              <View style={styles.rowContent}>
                <Text style={[styles.rowTitle, { color: colors.text }]}>{e.title}</Text>
                <Text style={[styles.rowTime, { color: colors.text + '66' }]}>{e.when}</Text>
              </View>
              <View style={styles.badge}>
                <Text style={styles.badgeText}>{e.material}</Text>
              </View>
              {e.points && (
                <View style={styles.points}>
                  <Text style={styles.pointsText}>+{e.points}</Text>
                </View>
              )}
            </View>
          ))}
        </View>
      )}

      <TouchableOpacity onPress={onViewAll} style={[styles.footer, { borderTopColor: colors.border }]}>
        <Text style={[styles.footerText, { color: '#10b981' }]}>View all activity</Text>
        <Ionicons name="chevron-forward" size={16} color="#10b981" />
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { borderRadius: 16, padding: 16, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 10, elevation: 4 },
  title: { fontSize: 18, fontWeight: '700', marginBottom: 12 },
  segmentedControl: { flexDirection: 'row', borderRadius: 8, padding: 2, marginBottom: 12 },
  segmentBtn: { flex: 1, paddingVertical: 6, alignItems: 'center', borderRadius: 6 },
  segmentText: { fontSize: 13, fontWeight: '600' },
  filtersRow: { gap: 6, marginBottom: 12 },
  filterChip: { paddingHorizontal: 10, paddingVertical: 6, borderRadius: 999 },
  filterText: { fontSize: 12, fontWeight: '600' },
  empty: { alignItems: 'center', paddingVertical: 32 },
  emptyText: { fontSize: 14, marginTop: 8, textAlign: 'center' },
  list: { gap: 10, marginBottom: 12 },
  row: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  thumb: { width: 40, height: 40, borderRadius: 8, alignItems: 'center', justifyContent: 'center', backgroundColor: '#f3f4f6' },
  thumbImg: { width: 40, height: 40, borderRadius: 8 },
  rowContent: { flex: 1 },
  rowTitle: { fontSize: 14, fontWeight: '600', marginBottom: 2 },
  rowTime: { fontSize: 12 },
  badge: { paddingHorizontal: 8, paddingVertical: 4, borderRadius: 999, backgroundColor: '#e0f2f1' },
  badgeText: { fontSize: 11, fontWeight: '600', color: '#00695c', textTransform: 'capitalize' },
  points: { paddingHorizontal: 6, paddingVertical: 2, borderRadius: 4, backgroundColor: '#fef3c7' },
  pointsText: { fontSize: 11, fontWeight: '700', color: '#92400e' },
  footer: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', paddingTop: 8, borderTopWidth: 1, gap: 4 },
  footerText: { fontSize: 14, fontWeight: '600' },
});

