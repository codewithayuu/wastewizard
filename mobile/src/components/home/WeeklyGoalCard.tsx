import { View, Text, Pressable, StyleSheet } from 'react-native';
import { useTheme } from '@react-navigation/native';
import { useAppStore } from '../../store/appStore';

function weeklyCount(activity: any[]) {
  const now = Date.now();
  const weekMs = 7 * 24 * 60 * 60 * 1000;
  return activity.filter((a) => {
    if (a.type !== 'scan' && a.type !== 'disposed') return false;
    const time = a.when === 'Just now' ? now : new Date(a.when).getTime();
    return now - time < weekMs;
  }).length;
}

export default function WeeklyGoalCard() {
  const { colors } = useTheme();
  const activity = useAppStore((s) => s.activity);
  const goal = useAppStore((s) => s.settings.weeklyGoal);
  const setWeeklyGoal = useAppStore((s) => s.setWeeklyGoal);
  const count = weeklyCount(activity);
  const pct = Math.min(1, count / goal);

  return (
    <View style={[styles.card, { backgroundColor: colors.card }]}>
      <Text style={[styles.title, { color: colors.text }]}>Weekly goal</Text>
      <Text style={[styles.subtitle, { color: colors.text + '99' }]}>
        {count}/{goal} items this week
      </Text>
      <View style={[styles.progressBg, { backgroundColor: colors.border }]}>
        <View style={[styles.progressBar, { width: `${pct * 100}%`, backgroundColor: colors.primary }]} />
      </View>
      <View style={styles.actions}>
        <Pressable onPress={() => setWeeklyGoal(Math.max(5, goal - 5))}>
          <Text style={{ color: colors.primary, fontWeight: '600' }}>-5</Text>
        </Pressable>
        <Pressable onPress={() => setWeeklyGoal(goal + 5)}>
          <Text style={{ color: colors.primary, fontWeight: '600' }}>+5</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { padding: 16, borderRadius: 12, shadowColor: '#000', shadowOpacity: 0.06, shadowRadius: 8, elevation: 2 },
  title: { fontSize: 16, fontWeight: '600' },
  subtitle: { marginTop: 4, fontSize: 13 },
  progressBg: { height: 10, borderRadius: 999, marginTop: 12 },
  progressBar: { height: 10, borderRadius: 999 },
  actions: { flexDirection: 'row', gap: 12, marginTop: 12 },
});

