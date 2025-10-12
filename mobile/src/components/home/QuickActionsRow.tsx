import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@react-navigation/native';

type Action = { icon: keyof typeof Ionicons.glyphMap; label: string; onPress: () => void };

export default function QuickActionsRow({ actions }: { actions: Action[] }) {
  const { colors } = useTheme();
  return (
    <View style={styles.row}>
      {actions.map((a, i) => (
        <TouchableOpacity key={i} style={[styles.btn, { backgroundColor: colors.card }]} onPress={a.onPress}>
          <Ionicons name={a.icon} size={28} color={colors.primary} />
          <Text style={[styles.label, { color: colors.text }]}>{a.label}</Text>
        </TouchableOpacity>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', justifyContent: 'space-between', gap: 8 },
  btn: { flex: 1, alignItems: 'center', justifyContent: 'center', borderRadius: 12, paddingVertical: 16, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 8, shadowOffset: { width: 0, height: 2 }, elevation: 4 },
  label: { marginTop: 6, fontSize: 12, fontWeight: '600' },
});

