import { View, Text, StyleSheet } from 'react-native';
import { useTheme } from '@react-navigation/native';
import { useAppStore, useLeague } from '../../store/appStore';

export default function LeagueCard() {
  const { colors } = useTheme();
  const points = useAppStore((s) => s.user.points);
  const { league, next, toNext } = useLeague();
  const progress = next ? Math.min(1, (points - league.min) / (next.min - league.min)) : 1;

  return (
    <View style={[styles.card, { backgroundColor: colors.card }]}>
      <Text style={[styles.title, { color: colors.text }]}>
        {league.char} {league.title}
      </Text>
      <Text style={[styles.subtitle, { color: colors.text + '99' }]}>
        {points} pts {next ? `• ${toNext} pts to next level` : '• Max level'}
      </Text>
      <View style={[styles.progressBg, { backgroundColor: colors.border }]}>
        <View style={[styles.progressBar, { width: `${progress * 100}%`, backgroundColor: league.color }]} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { padding: 16, borderRadius: 12, shadowColor: '#000', shadowOpacity: 0.06, shadowRadius: 8, elevation: 2 },
  title: { fontSize: 16, fontWeight: '600' },
  subtitle: { marginTop: 4, fontSize: 13 },
  progressBg: { height: 8, borderRadius: 999, marginTop: 12 },
  progressBar: { height: 8, borderRadius: 999 },
});

