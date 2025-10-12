import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@react-navigation/native';
import { useAppStore } from '../../store/appStore';
import { fmtMassKg } from '../../utils/units';
import { User, Badge } from '../../types/profile';

type Props = { user: User; badges: Badge[]; onViewBadges?: () => void };

export default function ImpactRewardsCard({ user, badges, onViewBadges }: Props) {
  const { colors } = useTheme();
  const units = useAppStore((s) => s.settings.units);
  const earnedBadges = badges.filter((b) => !b.locked).length;
  const nextIn = 4;

  return (
    <View style={[styles.card, { backgroundColor: colors.card }]}>
      <Text style={[styles.title, { color: colors.text }]}>Impact & Rewards</Text>

      <View style={styles.topRow}>
        <View style={styles.bigStat}>
          <Text style={[styles.bigNumber, { color: '#10b981' }]}>{user.points}</Text>
          <Text style={[styles.label, { color: colors.text + '99' }]}>Points</Text>
        </View>
        <View style={styles.statCol}>
          <View style={styles.stat}>
            <Ionicons name="flame" size={16} color="#f59e0b" />
            <Text style={[styles.statText, { color: colors.text }]}>Streak: {user.streakDays} days</Text>
          </View>
          <View style={styles.chipSmall}>
            <Text style={styles.chipSmallText}>Next badge in {nextIn} items</Text>
          </View>
        </View>
      </View>

      <View style={styles.chartBox}>
        <Text style={[styles.chartLabel, { color: colors.text + '99' }]}>Last 7 days</Text>
        <View style={[styles.chartPlaceholder, { backgroundColor: colors.border }] }>
          <Text style={[styles.chartPlaceholderText, { color: colors.text + '99' }]}>Mini chart (7-day bars)</Text>
        </View>
      </View>

      <View style={styles.badgesSection}>
        <Text style={[styles.sectionTitle, { color: colors.text }]}>Badges ({earnedBadges})</Text>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.badgesRow}>
          {badges.slice(0, 6).map((b) => (
            <View key={b.id} style={[styles.badge, b.locked && styles.badgeLocked]}>
              <Text style={styles.badgeIcon}>{b.icon}</Text>
              {b.locked && <Ionicons name="lock-closed" size={12} color="#999" style={styles.lock} />}
            </View>
          ))}
        </ScrollView>
      </View>

      <TouchableOpacity onPress={onViewBadges} style={[styles.btn, { backgroundColor: '#10b981' }]}>
        <Text style={[styles.btnText, { color: '#fff' }]}>View all badges</Text>
      </TouchableOpacity>

      <Text style={[styles.footer, { color: colors.text + '99' }]}>Estimated {fmtMassKg(1.8, units)} CO₂e avoided this week (Est.)</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { borderRadius: 16, padding: 16, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 10, elevation: 4 },
  title: { fontSize: 18, fontWeight: '700', marginBottom: 12 },
  topRow: { flexDirection: 'row', gap: 12, marginBottom: 12 },
  bigStat: { alignItems: 'center', paddingVertical: 8 },
  bigNumber: { fontSize: 32, fontWeight: '700' },
  label: { fontSize: 13, marginTop: 2 },
  statCol: { flex: 1, justifyContent: 'center', gap: 6 },
  stat: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  statText: { fontSize: 14, fontWeight: '600' },
  chipSmall: { paddingHorizontal: 8, paddingVertical: 4, borderRadius: 999, backgroundColor: '#fef3c7', alignSelf: 'flex-start' },
  chipSmallText: { fontSize: 12, fontWeight: '600', color: '#92400e' },
  chartBox: { marginBottom: 12 },
  chartLabel: { fontSize: 12, marginBottom: 4 },
  chartPlaceholder: { height: 80, borderRadius: 8, alignItems: 'center', justifyContent: 'center' },
  chartPlaceholderText: { fontSize: 12 },
  badgesSection: { marginBottom: 12 },
  sectionTitle: { fontSize: 14, fontWeight: '600', marginBottom: 8 },
  badgesRow: { gap: 8 },
  badge: { width: 56, height: 56, borderRadius: 28, backgroundColor: '#e0f2f1', alignItems: 'center', justifyContent: 'center', position: 'relative' },
  badgeLocked: { backgroundColor: '#e5e7eb' },
  badgeIcon: { fontSize: 24 },
  lock: { position: 'absolute', bottom: 4, right: 4 },
  btn: { paddingVertical: 10, alignItems: 'center', borderRadius: 8, marginBottom: 8 },
  btnText: { fontWeight: '600' },
  footer: { fontSize: 12, textAlign: 'center' },
});

