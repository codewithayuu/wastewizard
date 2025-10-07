import { View, Text, StyleSheet } from 'react-native';
import { useTheme } from '@react-navigation/native';
import { useAppStore, Material } from '../../store/appStore';

const COLORS: Record<Material, string> = {
  plastic: '#0EA5E9',
  paper: '#3B82F6',
  glass: '#06B6D4',
  metal: '#F59E0B',
  organic: '#16A34A',
  'e-waste': '#8B5CF6',
  hazardous: '#EF4444',
};

const LABELS: Record<Material, string> = {
  plastic: 'Plastic',
  paper: 'Paper',
  glass: 'Glass',
  metal: 'Metal',
  organic: 'Organic',
  'e-waste': 'E‑waste',
  hazardous: 'Hazardous',
};

export default function SegregationBars() {
  const { colors } = useTheme();
  const counts = useAppStore((s) => s.user.segregatedCounts);
  const total = Object.values(counts).reduce((a, b) => a + b, 0) || 1;

  return (
    <View style={[styles.card, { backgroundColor: colors.card }]}>
      <Text style={[styles.title, { color: colors.text }]}>Your segregation</Text>
      <View style={styles.list}>
        {(Object.entries(counts) as [Material, number][]).map(([key, n]) => {
          const pct = (Number(n) / total) * 100;
          return (
            <View key={key}>
              <View style={styles.row}>
                <Text style={{ color: colors.text }}>{LABELS[key]}</Text>
                <Text style={{ color: colors.text + '99' }}>
                  {n} • {Math.round(pct)}%
                </Text>
              </View>
              <View style={[styles.barBg, { backgroundColor: colors.border }]}>
                <View style={[styles.bar, { width: `${pct}%`, backgroundColor: COLORS[key] }]} />
              </View>
            </View>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { padding: 16, borderRadius: 12, shadowColor: '#000', shadowOpacity: 0.06, shadowRadius: 8, elevation: 2 },
  title: { fontSize: 16, fontWeight: '600' },
  list: { marginTop: 12, gap: 10 },
  row: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 4 },
  barBg: { height: 8, borderRadius: 999 },
  bar: { height: 8, borderRadius: 999 },
});

