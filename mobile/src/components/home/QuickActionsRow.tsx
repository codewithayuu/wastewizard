import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

type Action = { icon: keyof typeof Ionicons.glyphMap; label: string; onPress: () => void };

export default function QuickActionsRow({ actions }: { actions: Action[] }) {
  return (
    <View style={styles.row}>
      {actions.map((a, i) => (
        <TouchableOpacity key={i} style={styles.btn} onPress={a.onPress}>
          <Ionicons name={a.icon} size={28} color="#10b981" />
          <Text style={styles.label}>{a.label}</Text>
        </TouchableOpacity>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', justifyContent: 'space-between', gap: 8 },
  btn: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: '#fff', borderRadius: 12, paddingVertical: 16, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 8, shadowOffset: { width: 0, height: 2 }, elevation: 4 },
  label: { marginTop: 6, fontSize: 12, fontWeight: '600', color: '#333' },
});

