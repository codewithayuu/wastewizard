import { View, Text, StyleSheet, TouchableOpacity, FlatList } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

type Tip = { id: string; title: string; body: string; icon?: keyof typeof Ionicons.glyphMap };

export default function TipsCarousel({ tips, onPressTip }: { tips: Tip[]; onPressTip?: (tip: Tip) => void }) {
  return (
    <View>
      <Text style={styles.heading}>Tips for you</Text>
      <FlatList
        data={tips}
        horizontal
        showsHorizontalScrollIndicator={false}
        keyExtractor={(t) => t.id}
        renderItem={({ item }) => (
          <TouchableOpacity style={styles.card} onPress={() => onPressTip?.(item)}>
            <Ionicons name={item.icon || 'bulb-outline'} size={32} color="#10b981" />
            <Text style={styles.title}>{item.title}</Text>
            <Text style={styles.body} numberOfLines={2}>{item.body}</Text>
            <Text style={styles.cta}>Read</Text>
          </TouchableOpacity>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  heading: { fontSize: 16, fontWeight: '700', marginBottom: 8, paddingHorizontal: 4 },
  card: { width: 200, backgroundColor: '#fff', borderRadius: 12, padding: 12, marginRight: 12, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 8, shadowOffset: { width: 0, height: 2 }, elevation: 4 },
  title: { fontSize: 14, fontWeight: '700', marginTop: 8 },
  body: { fontSize: 12, color: '#666', marginTop: 4 },
  cta: { fontSize: 12, color: '#10b981', fontWeight: '600', marginTop: 8 },
});

