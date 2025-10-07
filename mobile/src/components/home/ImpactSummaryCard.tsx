import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import Svg, { Circle } from 'react-native-svg';

type Metrics = { items: number; co2eKg: number; waterL?: number; energyKwh?: number; eq?: { drivingKm?: number; phoneCharges?: number } };

type Props = { period?: 'week' | 'month' | 'all'; goal?: number; metrics: Metrics; onPressDetails?: () => void };

export default function ImpactSummaryCard({ period = 'week', goal = 20, metrics, onPressDetails }: Props) {
  const progress = Math.min(metrics.items / goal, 1);
  const radius = 30;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference * (1 - progress);

  return (
    <TouchableOpacity style={styles.card} onPress={onPressDetails} activeOpacity={0.8}>
      <View style={styles.left}>
        <Svg width={64} height={64}>
          <Circle cx={32} cy={32} r={radius} stroke="#e0e0e0" strokeWidth={6} fill="none" />
          <Circle cx={32} cy={32} r={radius} stroke="#10b981" strokeWidth={6} fill="none" strokeDasharray={circumference} strokeDashoffset={strokeDashoffset} strokeLinecap="round" rotation="-90" origin="32, 32" />
        </Svg>
        <Text style={styles.progressText}>{metrics.items}/{goal}</Text>
      </View>
      <View style={styles.right}>
        <Text style={styles.title}>Your impact this {period}</Text>
        <Text style={styles.stat}>{metrics.items} items sorted • {metrics.co2eKg.toFixed(1)} kg CO₂e avoided</Text>
        {metrics.eq?.drivingKm ? <Text style={styles.sub}>≈ {metrics.eq.drivingKm} km of driving not done</Text> : null}
        <View style={styles.chips}>
          <View style={styles.chip}><Text style={styles.chipText}>CO₂e</Text></View>
          {metrics.waterL ? <View style={styles.chip}><Text style={styles.chipText}>Water</Text></View> : null}
          {metrics.energyKwh ? <View style={styles.chip}><Text style={styles.chipText}>Energy</Text></View> : null}
        </View>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: { flexDirection: 'row', backgroundColor: '#fff', borderRadius: 12, padding: 16, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 8, shadowOffset: { width: 0, height: 2 }, elevation: 4 },
  left: { marginRight: 16, alignItems: 'center', justifyContent: 'center' },
  progressText: { position: 'absolute', fontSize: 14, fontWeight: '700', color: '#10b981' },
  right: { flex: 1 },
  title: { fontSize: 16, fontWeight: '700', marginBottom: 4 },
  stat: { fontSize: 14, color: '#333' },
  sub: { fontSize: 12, color: '#666', marginTop: 2 },
  chips: { flexDirection: 'row', marginTop: 8, gap: 6 },
  chip: { backgroundColor: '#e0f2f1', paddingHorizontal: 8, paddingVertical: 4, borderRadius: 999 },
  chipText: { fontSize: 11, fontWeight: '600', color: '#00695c' },
});

