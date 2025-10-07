import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

type Props = { userName?: string; points?: number; streak?: number; onPressAvatar?: () => void; onPressPoints?: () => void };

export default function HeaderCard({ userName = 'User', points = 0, streak = 0, onPressAvatar, onPressPoints }: Props) {
  return (
    <View style={styles.card}>
      <TouchableOpacity onPress={onPressAvatar} style={styles.avatar}>
        <Ionicons name="person-circle-outline" size={48} color="#10b981" />
      </TouchableOpacity>
      <View style={styles.text}>
        <Text style={styles.greeting}>Hi, {userName}!</Text>
        <Text style={styles.subtext}>Let's make an impact today</Text>
      </View>
      <TouchableOpacity onPress={onPressPoints} style={styles.chip}>
        <Text style={styles.chipText}>{points} pts</Text>
      </TouchableOpacity>
      <View style={styles.chip}>
        <Text style={styles.chipText}>{streak}🔥</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#fff', borderRadius: 12, padding: 12, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 8, shadowOffset: { width: 0, height: 2 }, elevation: 4 },
  avatar: { marginRight: 12 },
  text: { flex: 1 },
  greeting: { fontSize: 18, fontWeight: '700' },
  subtext: { fontSize: 14, color: '#666', marginTop: 2 },
  chip: { backgroundColor: '#e0f2f1', paddingHorizontal: 10, paddingVertical: 6, borderRadius: 999, marginLeft: 8 },
  chipText: { fontSize: 12, fontWeight: '600', color: '#00695c' },
});

