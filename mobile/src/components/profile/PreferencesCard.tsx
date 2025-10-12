import { View, Text, StyleSheet, TouchableOpacity, Switch } from 'react-native';
import { useEffect, useState } from 'react';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@react-navigation/native';
import { User } from '../../types/profile';
import TextPromptModal from '../common/TextPromptModal';
import { getStoredApiBaseUrl, setStoredApiBaseUrl } from '../../storage';

type Props = { user: User; onUpdate?: (prefs: User['preferences']) => void };

export default function PreferencesCard({ user, onUpdate }: Props) {
  const { colors } = useTheme();
  const [apiUrl, setApiUrl] = useState<string | null>(null);
  const [editApiOpen, setEditApiOpen] = useState(false);
  // Fallback ensures UI doesn't crash if preferences are temporarily undefined
  const preferences = user.preferences ?? {
    language: 'English',
    theme: 'system' as const,
    units: 'metric' as const,
    notifications: { reminders: true, tips: true, achievements: true },
    accessibility: { largerText: false, reducedMotion: false, haptics: true },
  };

  const toggleNotif = (key: keyof typeof preferences.notifications) => {
    const updated = { ...preferences, notifications: { ...preferences.notifications, [key]: !preferences.notifications[key] } };
    onUpdate?.(updated);
  };

  const toggleAccess = (key: keyof typeof preferences.accessibility) => {
    const updated = { ...preferences, accessibility: { ...preferences.accessibility, [key]: !preferences.accessibility[key] } };
    onUpdate?.(updated);
  };

  const availableLanguages = ['English', 'Spanish', 'French'] as const;
  const onPressLanguage = () => {
    const idx = availableLanguages.indexOf(preferences.language as any);
    const next = availableLanguages[(idx + 1) % availableLanguages.length];
    onUpdate?.({ ...preferences, language: next });
  };

  useEffect(() => {
    (async () => {
      const v = await getStoredApiBaseUrl();
      setApiUrl(v);
    })();
  }, []);

  return (
    <View style={[styles.card, { backgroundColor: colors.card }] }>
      <Text style={[styles.title, { color: colors.text }]}>Preferences</Text>

      <TouchableOpacity style={[styles.row, { borderBottomColor: colors.border }]} onPress={onPressLanguage}>
        <Ionicons name="language-outline" size={20} color={colors.text + '99'} />
        <Text style={[styles.rowText, { color: colors.text }]}>Language</Text>
        <Text style={[styles.rowValue, { color: colors.text + '66' }]}>{preferences.language}</Text>
        <Ionicons name="chevron-forward" size={18} color={colors.text + '66'} />
      </TouchableOpacity>

      <View style={styles.section}>
        <Text style={[styles.sectionTitle, { color: colors.text + '99' }]}>Theme</Text>
        <View style={[styles.segmentedControl, { backgroundColor: colors.border }]}>
          {['System', 'Light', 'Dark'].map((t) => {
            const key = t.toLowerCase() as 'system' | 'light' | 'dark';
            return (
              <TouchableOpacity key={t} onPress={() => onUpdate?.({ ...preferences, theme: key })} style={[styles.segmentBtn, preferences.theme === key && { backgroundColor: colors.card }]}>
                <Text style={[styles.segmentText, { color: colors.text + '99' }, preferences.theme === key && { color: colors.text }]}>{t}</Text>
              </TouchableOpacity>
            );
          })}
        </View>
      </View>

      <View style={styles.section}>
        <Text style={[styles.sectionTitle, { color: colors.text + '99' }]}>Units</Text>
        <View style={[styles.segmentedControl, { backgroundColor: colors.border }]}>
          {['Metric', 'Imperial'].map((u) => {
            const key = u.toLowerCase() as 'metric' | 'imperial';
            return (
              <TouchableOpacity key={u} onPress={() => onUpdate?.({ ...preferences, units: key })} style={[styles.segmentBtn, preferences.units === key && { backgroundColor: colors.card }]}>
                <Text style={[styles.segmentText, { color: colors.text + '99' }, preferences.units === key && { color: colors.text }]}>{u}</Text>
              </TouchableOpacity>
            );
          })}
        </View>
      </View>

      <View style={styles.section}>
        <Text style={[styles.sectionTitle, { color: colors.text + '99' }]}>Notifications</Text>
        <View style={styles.toggleRow}>
          <Text style={[styles.toggleLabel, { color: colors.text } ]}>Reminders</Text>
          <Switch value={preferences.notifications.reminders} onValueChange={() => toggleNotif('reminders')} />
        </View>
        <View style={styles.toggleRow}>
          <Text style={[styles.toggleLabel, { color: colors.text } ]}>Achievements</Text>
          <Switch value={preferences.notifications.achievements} onValueChange={() => toggleNotif('achievements')} />
        </View>
        <View style={styles.toggleRow}>
          <Text style={[styles.toggleLabel, { color: colors.text } ]}>Tips</Text>
          <Switch value={preferences.notifications.tips} onValueChange={() => toggleNotif('tips')} />
        </View>
      </View>

      <View style={styles.section}>
        <Text style={[styles.sectionTitle, { color: colors.text + '99' }]}>Developer</Text>
        <TouchableOpacity style={[styles.row, { borderBottomColor: colors.border }]} onPress={() => setEditApiOpen(true)}>
          <Ionicons name="globe-outline" size={20} color={colors.text + '99'} />
          <Text style={[styles.rowText, { color: colors.text }]}>Backend URL</Text>
          <Text style={[styles.rowValue, { color: colors.text + '66' }]}>{apiUrl || '(default)'}</Text>
          <Ionicons name="chevron-forward" size={18} color={colors.text + '66'} />
        </TouchableOpacity>
      </View>

      {/* Accessibility section removed per request */}

      <TextPromptModal
        visible={editApiOpen}
        title="Backend URL"
        placeholder="e.g. http://192.168.1.5:8000"
        initialValue={apiUrl || ''}
        confirmLabel="Save"
        onCancel={() => setEditApiOpen(false)}
        onSubmit={async (val) => { await setStoredApiBaseUrl(val.trim()); setApiUrl(val.trim()); setEditApiOpen(false); }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  card: { borderRadius: 16, padding: 16, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 10, elevation: 4 },
  title: { fontSize: 18, fontWeight: '700', marginBottom: 12 },
  row: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, borderBottomWidth: 1, gap: 10 },
  rowText: { flex: 1, fontSize: 15 },
  rowValue: { fontSize: 14 },
  section: { marginTop: 12 },
  sectionTitle: { fontSize: 14, fontWeight: '600', marginBottom: 8 },
  segmentedControl: { flexDirection: 'row', borderRadius: 8, padding: 2 },
  segmentBtn: { flex: 1, paddingVertical: 6, alignItems: 'center', borderRadius: 6 },
  segmentText: { fontSize: 13, fontWeight: '600' },
  toggleRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingVertical: 10 },
  toggleLabel: { fontSize: 15 },
});

