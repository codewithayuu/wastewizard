import { View, Text, StyleSheet, TouchableOpacity, Switch } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { User } from '../../types/profile';

type Props = { user: User; onUpdate?: (prefs: User['preferences']) => void };

export default function PreferencesCard({ user, onUpdate }: Props) {
  const { preferences } = user;

  const toggleNotif = (key: keyof typeof preferences.notifications) => {
    const updated = { ...preferences, notifications: { ...preferences.notifications, [key]: !preferences.notifications[key] } };
    onUpdate?.(updated);
  };

  const toggleAccess = (key: keyof typeof preferences.accessibility) => {
    const updated = { ...preferences, accessibility: { ...preferences.accessibility, [key]: !preferences.accessibility[key] } };
    onUpdate?.(updated);
  };

  return (
    <View style={styles.card}>
      <Text style={styles.title}>Preferences</Text>

      <TouchableOpacity style={styles.row}>
        <Ionicons name="language-outline" size={20} color="#666" />
        <Text style={styles.rowText}>Language</Text>
        <Text style={styles.rowValue}>{preferences.language}</Text>
        <Ionicons name="chevron-forward" size={18} color="#999" />
      </TouchableOpacity>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Theme</Text>
        <View style={styles.segmentedControl}>
          {['System', 'Light', 'Dark'].map((t) => {
            const key = t.toLowerCase() as 'system' | 'light' | 'dark';
            return (
              <TouchableOpacity key={t} onPress={() => onUpdate?.({ ...preferences, theme: key })} style={[styles.segmentBtn, preferences.theme === key && styles.segmentBtnActive]}>
                <Text style={[styles.segmentText, preferences.theme === key && styles.segmentTextActive]}>{t}</Text>
              </TouchableOpacity>
            );
          })}
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Units</Text>
        <View style={styles.segmentedControl}>
          {['Metric', 'Imperial'].map((u) => {
            const key = u.toLowerCase() as 'metric' | 'imperial';
            return (
              <TouchableOpacity key={u} onPress={() => onUpdate?.({ ...preferences, units: key })} style={[styles.segmentBtn, preferences.units === key && styles.segmentBtnActive]}>
                <Text style={[styles.segmentText, preferences.units === key && styles.segmentTextActive]}>{u}</Text>
              </TouchableOpacity>
            );
          })}
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Notifications</Text>
        <View style={styles.toggleRow}>
          <Text style={styles.toggleLabel}>Reminders</Text>
          <Switch value={preferences.notifications.reminders} onValueChange={() => toggleNotif('reminders')} />
        </View>
        <View style={styles.toggleRow}>
          <Text style={styles.toggleLabel}>Achievements</Text>
          <Switch value={preferences.notifications.achievements} onValueChange={() => toggleNotif('achievements')} />
        </View>
        <View style={styles.toggleRow}>
          <Text style={styles.toggleLabel}>Tips</Text>
          <Switch value={preferences.notifications.tips} onValueChange={() => toggleNotif('tips')} />
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Accessibility</Text>
        <View style={styles.toggleRow}>
          <Text style={styles.toggleLabel}>Larger text</Text>
          <Switch value={preferences.accessibility.largerText} onValueChange={() => toggleAccess('largerText')} />
        </View>
        <View style={styles.toggleRow}>
          <Text style={styles.toggleLabel}>Reduced motion</Text>
          <Switch value={preferences.accessibility.reducedMotion} onValueChange={() => toggleAccess('reducedMotion')} />
        </View>
        <View style={styles.toggleRow}>
          <Text style={styles.toggleLabel}>Haptics</Text>
          <Switch value={preferences.accessibility.haptics} onValueChange={() => toggleAccess('haptics')} />
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: '#fff', borderRadius: 16, padding: 16, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 10, elevation: 4 },
  title: { fontSize: 18, fontWeight: '700', marginBottom: 12 },
  row: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: '#e5e7eb', gap: 10 },
  rowText: { flex: 1, fontSize: 15 },
  rowValue: { fontSize: 14, color: '#999' },
  section: { marginTop: 12 },
  sectionTitle: { fontSize: 14, fontWeight: '600', marginBottom: 8, color: '#666' },
  segmentedControl: { flexDirection: 'row', backgroundColor: '#f3f4f6', borderRadius: 8, padding: 2 },
  segmentBtn: { flex: 1, paddingVertical: 6, alignItems: 'center', borderRadius: 6 },
  segmentBtnActive: { backgroundColor: '#fff' },
  segmentText: { fontSize: 13, color: '#666', fontWeight: '600' },
  segmentTextActive: { color: '#111827' },
  toggleRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingVertical: 10 },
  toggleLabel: { fontSize: 15 },
});

