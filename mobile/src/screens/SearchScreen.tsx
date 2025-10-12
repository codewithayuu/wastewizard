import { useState } from 'react';
import { View, Text, TextInput, StyleSheet, FlatList, TouchableOpacity } from 'react-native';
import { useTheme } from '@react-navigation/native';

export default function SearchScreen({ navigation }: any) {
  const { colors } = useTheme();
  const [q, setQ] = useState('');
  const [results, setResults] = useState<{ id: string; title: string; material: string }[]>([]);

  const onSearch = () => {
    // Placeholder local search: empty for now
    setResults([]);
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.box, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <TextInput
          placeholder="Search items (e.g. bottle, can, bag)"
          placeholderTextColor={colors.text + '66'}
          value={q}
          onChangeText={setQ}
          onSubmitEditing={onSearch}
          style={[styles.input, { color: colors.text }]}
        />
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.close}>
          <Text style={{ color: colors.primary, fontWeight: '700' }}>Close</Text>
        </TouchableOpacity>
      </View>
      <FlatList
        data={results}
        keyExtractor={(i) => i.id}
        contentContainerStyle={styles.list}
        ListEmptyComponent={<Text style={{ color: colors.text + '99', textAlign: 'center', marginTop: 20 }}>No results yet.</Text>}
        renderItem={({ item }) => (
          <View style={[styles.row, { backgroundColor: colors.card }] }>
            <Text style={{ color: colors.text, fontWeight: '600' }}>{item.title}</Text>
            <Text style={{ color: colors.text + '66' }}>{item.material}</Text>
          </View>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16 },
  box: { flexDirection: 'row', alignItems: 'center', borderRadius: 10, borderWidth: 1, paddingHorizontal: 12, paddingVertical: 8, marginBottom: 12 },
  input: { flex: 1, fontSize: 16, paddingVertical: 8 },
  close: { marginLeft: 12 },
  list: { gap: 8 },
  row: { padding: 12, borderRadius: 10 },
});
