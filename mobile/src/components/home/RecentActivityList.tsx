import { View, Text, StyleSheet, TouchableOpacity, FlatList } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

type Item = { id: string; title: string; category: string; time: string; thumbnail?: string };

export default function RecentActivityList({ items, limit = 6, onPressItem }: { items: Item[]; limit?: number; onPressItem?: (item: Item) => void }) {
  const data = items.slice(0, limit);
  if (data.length === 0) {
    return (
      <View style={styles.empty}>
        <Text style={styles.emptyText}>No scans yet—try Scan to identify your first item.</Text>
      </View>
    );
  }
  return (
    <View style={styles.card}>
      <Text style={styles.title}>Recent Activity</Text>
      <FlatList
        data={data}
        keyExtractor={(i) => i.id}
        scrollEnabled={false}
        renderItem={({ item }) => (
          <TouchableOpacity style={styles.row} onPress={() => onPressItem?.(item)}>
            <View style={styles.thumb}>
              <Ionicons name="image-outline" size={24} color="#999" />
            </View>
            <View style={styles.text}>
              <Text style={styles.itemTitle}>{item.title}</Text>
              <Text style={styles.time}>{item.time}</Text>
            </View>
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{item.category}</Text>
            </View>
          </TouchableOpacity>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: '#fff', borderRadius: 12, padding: 12, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 8, shadowOffset: { width: 0, height: 2 }, elevation: 4 },
  title: { fontSize: 16, fontWeight: '700', marginBottom: 8 },
  row: { flexDirection: 'row', alignItems: 'center', paddingVertical: 8 },
  thumb: { width: 40, height: 40, borderRadius: 8, backgroundColor: '#f0f0f0', alignItems: 'center', justifyContent: 'center', marginRight: 12 },
  text: { flex: 1 },
  itemTitle: { fontSize: 14, fontWeight: '600', color: '#333' },
  time: { fontSize: 12, color: '#999', marginTop: 2 },
  badge: { backgroundColor: '#e0f2f1', paddingHorizontal: 8, paddingVertical: 4, borderRadius: 999 },
  badgeText: { fontSize: 11, fontWeight: '600', color: '#00695c' },
  empty: { backgroundColor: '#fff', borderRadius: 12, padding: 20, alignItems: 'center', shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 8, shadowOffset: { width: 0, height: 2 }, elevation: 4 },
  emptyText: { fontSize: 14, color: '#666', textAlign: 'center' },
});

