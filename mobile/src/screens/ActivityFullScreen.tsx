import { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, FlatList, TextInput, Image } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { ActivityEntry } from '../types/profile';

const mockActivity: ActivityEntry[] = [
  { id: '1', type: 'scan', title: 'Plastic bottle', material: 'plastic', when: '2h ago', points: 10 },
  { id: '2', type: 'disposed', title: 'Glass jar', material: 'glass', when: '5h ago', points: 15 },
  { id: '3', type: 'bookmark', title: 'Aluminum can', material: 'metal', when: '1d ago', points: 10 },
  { id: '4', type: 'scan', title: 'Paper bag', material: 'paper', when: '2d ago', points: 8 },
  { id: '5', type: 'disposed', title: 'Cardboard box', material: 'paper', when: '3d ago', points: 12 },
];

export default function ActivityFullScreen({ navigation }: any) {
  const [search, setSearch] = useState('');
  const [sort, setSort] = useState<'newest' | 'oldest' | 'highest'>('newest');

  const filtered = mockActivity.filter((e) => e.title.toLowerCase().includes(search.toLowerCase()));

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={24} color="#333" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Activity</Text>
        <TouchableOpacity style={styles.iconBtn}>
          <Ionicons name="download-outline" size={22} color="#333" />
        </TouchableOpacity>
      </View>

      <View style={styles.searchBox}>
        <Ionicons name="search-outline" size={20} color="#999" />
        <TextInput placeholder="Search activity" value={search} onChangeText={setSearch} style={styles.searchInput} />
      </View>

      <View style={styles.sortRow}>
        {(['newest', 'oldest', 'highest'] as const).map((s) => (
          <TouchableOpacity key={s} onPress={() => setSort(s)} style={[styles.sortChip, sort === s && styles.sortChipActive]}>
            <Text style={[styles.sortText, sort === s && styles.sortTextActive]}>{s.charAt(0).toUpperCase() + s.slice(1)}</Text>
          </TouchableOpacity>
        ))}
      </View>

      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <View style={styles.row}>
            <View style={styles.thumb}>
              {item.thumb ? <Image source={{ uri: item.thumb }} style={styles.thumbImg} /> : <Ionicons name="cube-outline" size={20} color="#999" />}
            </View>
            <View style={styles.rowContent}>
              <Text style={styles.rowTitle}>{item.title}</Text>
              <Text style={styles.rowTime}>{item.when}</Text>
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
            <Ionicons name="leaf-outline" size={48} color="#d1d5db" />
            <Text style={styles.emptyText}>No activity found.</Text>
          </View>
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f9fafb' },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 16, paddingVertical: 12, backgroundColor: '#fff', borderBottomWidth: 1, borderBottomColor: '#e5e7eb' },
  backBtn: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
  headerTitle: { fontSize: 18, fontWeight: '700', flex: 1, textAlign: 'center' },
  iconBtn: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
  searchBox: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#fff', paddingHorizontal: 16, paddingVertical: 10, gap: 8, borderBottomWidth: 1, borderBottomColor: '#e5e7eb' },
  searchInput: { flex: 1, fontSize: 15 },
  sortRow: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, paddingVertical: 10, backgroundColor: '#fff', borderBottomWidth: 1, borderBottomColor: '#e5e7eb' },
  sortChip: { paddingHorizontal: 12, paddingVertical: 6, borderRadius: 999, backgroundColor: '#f3f4f6' },
  sortChipActive: { backgroundColor: '#e0f2f1' },
  sortText: { fontSize: 12, fontWeight: '600', color: '#666' },
  sortTextActive: { color: '#00695c' },
  list: { padding: 16, gap: 12 },
  row: { flexDirection: 'row', alignItems: 'center', gap: 10, backgroundColor: '#fff', padding: 12, borderRadius: 12, shadowColor: '#000', shadowOpacity: 0.04, shadowRadius: 6, elevation: 2 },
  thumb: { width: 40, height: 40, borderRadius: 8, backgroundColor: '#f3f4f6', alignItems: 'center', justifyContent: 'center' },
  thumbImg: { width: 40, height: 40, borderRadius: 8 },
  rowContent: { flex: 1 },
  rowTitle: { fontSize: 14, fontWeight: '600', marginBottom: 2 },
  rowTime: { fontSize: 12, color: '#999' },
  badge: { paddingHorizontal: 8, paddingVertical: 4, borderRadius: 999, backgroundColor: '#e0f2f1' },
  badgeText: { fontSize: 11, fontWeight: '600', color: '#00695c', textTransform: 'capitalize' },
  points: { paddingHorizontal: 6, paddingVertical: 2, borderRadius: 4, backgroundColor: '#fef3c7' },
  pointsText: { fontSize: 11, fontWeight: '700', color: '#92400e' },
  empty: { alignItems: 'center', paddingVertical: 64 },
  emptyText: { fontSize: 14, color: '#999', marginTop: 8 },
});

