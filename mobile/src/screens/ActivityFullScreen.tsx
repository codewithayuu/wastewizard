import { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, FlatList, TextInput, Image } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@react-navigation/native';
import { ActivityEntry } from '../types/profile';
import { useAppStore } from '../store/appStore';

// Activity is read from the global store

export default function ActivityFullScreen({ navigation }: any) {
  const { colors } = useTheme();
  const [search, setSearch] = useState('');
  const [sort, setSort] = useState<'newest' | 'oldest' | 'highest'>('newest');
  const activity = useAppStore((s) => s.activity);

  const filtered = activity.filter((e) => e.title.toLowerCase().includes(search.toLowerCase()));

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { backgroundColor: colors.card, borderBottomColor: colors.border }]}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={24} color={colors.text} />
        </TouchableOpacity>
        <Text style={[styles.headerTitle, { color: colors.text }]}>Activity</Text>
        <TouchableOpacity style={styles.iconBtn}>
          <Ionicons name="download-outline" size={22} color={colors.text} />
        </TouchableOpacity>
      </View>

      <View style={[styles.searchBox, { backgroundColor: colors.card, borderBottomColor: colors.border }]}>
        <Ionicons name="search-outline" size={20} color={colors.text + '66'} />
        <TextInput placeholder="Search activity" placeholderTextColor={colors.text + '66'} value={search} onChangeText={setSearch} style={[styles.searchInput, { color: colors.text }]} />
      </View>

      <View style={[styles.sortRow, { backgroundColor: colors.card, borderBottomColor: colors.border }]}>
        {(['newest', 'oldest', 'highest'] as const).map((s) => (
          <TouchableOpacity key={s} onPress={() => setSort(s)} style={[styles.sortChip, { backgroundColor: colors.border }, sort === s && { backgroundColor: '#e0f2f1' }]}>
            <Text style={[styles.sortText, { color: colors.text + '99' }, sort === s && { color: '#00695c' }]}>{s.charAt(0).toUpperCase() + s.slice(1)}</Text>
          </TouchableOpacity>
        ))}
      </View>

      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <View style={[styles.row, { backgroundColor: colors.card }]}>
            <View style={[styles.thumb, { backgroundColor: colors.border }]}>
              {item.thumb ? <Image source={{ uri: item.thumb }} style={styles.thumbImg} /> : <Ionicons name="cube-outline" size={20} color={colors.text + '66'} />}
            </View>
            <View style={styles.rowContent}>
              <Text style={[styles.rowTitle, { color: colors.text }]}>{item.title}</Text>
              <Text style={[styles.rowTime, { color: colors.text + '66' }]}>{item.when}</Text>
            </View>
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{item.material}</Text>
            </View>
            {item.points && (
              <View style={styles.points}>
                <Text style={styles.pointsText}>+{item.points}</Text>
              </View>
            )}
          </View>
        )}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Ionicons name="leaf-outline" size={48} color={colors.text + '55'} />
            <Text style={[styles.emptyText, { color: colors.text + '99' }]}>No activity found.</Text>
          </View>
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 16, paddingVertical: 12, borderBottomWidth: 1 },
  backBtn: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
  headerTitle: { fontSize: 18, fontWeight: '700', flex: 1, textAlign: 'center' },
  iconBtn: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
  searchBox: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 10, gap: 8, borderBottomWidth: 1 },
  searchInput: { flex: 1, fontSize: 15 },
  sortRow: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, paddingVertical: 10, borderBottomWidth: 1 },
  sortChip: { paddingHorizontal: 12, paddingVertical: 6, borderRadius: 999 },
  sortChipActive: { backgroundColor: '#e0f2f1' },
  sortText: { fontSize: 12, fontWeight: '600' },
  sortTextActive: { color: '#00695c' },
  list: { padding: 16, gap: 12 },
  row: { flexDirection: 'row', alignItems: 'center', gap: 10, padding: 12, borderRadius: 12, shadowColor: '#000', shadowOpacity: 0.04, shadowRadius: 6, elevation: 2 },
  thumb: { width: 40, height: 40, borderRadius: 8, alignItems: 'center', justifyContent: 'center' },
  thumbImg: { width: 40, height: 40, borderRadius: 8 },
  rowContent: { flex: 1 },
  rowTitle: { fontSize: 14, fontWeight: '600', marginBottom: 2 },
  rowTime: { fontSize: 12 },
  badge: { paddingHorizontal: 8, paddingVertical: 4, borderRadius: 999, backgroundColor: '#e0f2f1' },
  badgeText: { fontSize: 11, fontWeight: '600', color: '#00695c', textTransform: 'capitalize' },
  points: { paddingHorizontal: 6, paddingVertical: 2, borderRadius: 4, backgroundColor: '#fef3c7' },
  pointsText: { fontSize: 11, fontWeight: '700', color: '#92400e' },
  empty: { alignItems: 'center', paddingVertical: 64 },
  emptyText: { fontSize: 14, marginTop: 8 },
});

