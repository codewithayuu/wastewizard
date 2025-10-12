import { View, Text, StyleSheet, Modal, TouchableOpacity, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@react-navigation/native';
import { useAppStore } from '../../store/appStore';
import { fmtMassKg, fmtVolumeL, fmtEnergyKwh, fmtDistanceKm } from '../../utils/units';

type Props = { visible: boolean; onClose: () => void; metrics: { items: number; co2eKg: number; waterL?: number; energyKwh?: number; eq?: { drivingKm?: number; phoneCharges?: number } } };

export default function ImpactBreakdownSheet({ visible, onClose, metrics }: Props) {
  const { colors } = useTheme();
  const units = useAppStore((s) => s.settings.units);
  return (
    <Modal visible={visible} animationType="slide" transparent onRequestClose={onClose}>
      <View style={styles.backdrop}>
        <View style={[styles.sheet, { backgroundColor: colors.card }]}>
          <View style={styles.header}>
            <Text style={[styles.title, { color: colors.text }]}>Your Impact Breakdown</Text>
            <TouchableOpacity onPress={onClose}>
              <Ionicons name="close" size={24} color={colors.text} />
            </TouchableOpacity>
          </View>
          <ScrollView>
            <View style={[styles.stat, { borderBottomColor: colors.border }]}>
              <Text style={[styles.statLabel, { color: colors.text + '99' }]}>Items sorted</Text>
              <Text style={[styles.statValue, { color: colors.text }]}>{metrics.items}</Text>
            </View>
            <View style={[styles.stat, { borderBottomColor: colors.border }]}>
              <Text style={[styles.statLabel, { color: colors.text + '99' }]}>CO₂e avoided</Text>
              <Text style={[styles.statValue, { color: colors.text }]}>{fmtMassKg(metrics.co2eKg, units)}</Text>
            </View>
            {metrics.waterL ? (
              <View style={[styles.stat, { borderBottomColor: colors.border }]}>
                <Text style={[styles.statLabel, { color: colors.text + '99' }]}>Water saved</Text>
                <Text style={[styles.statValue, { color: colors.text }]}>{fmtVolumeL(metrics.waterL, units)}</Text>
              </View>
            ) : null}
            {metrics.energyKwh ? (
              <View style={[styles.stat, { borderBottomColor: colors.border }]}>
                <Text style={[styles.statLabel, { color: colors.text + '99' }]}>Energy saved</Text>
                <Text style={[styles.statValue, { color: colors.text }]}>{fmtEnergyKwh(metrics.energyKwh)}</Text>
              </View>
            ) : null}
            {metrics.eq ? (
              <View style={[styles.eq, { backgroundColor: colors.border + '44' }]}>
                <Text style={[styles.eqTitle, { color: colors.text }]}>Equivalents</Text>
                {metrics.eq.drivingKm ? <Text style={[styles.eqText, { color: colors.text }]}>
                  • {fmtMassKg(metrics.co2eKg, units)} ≈ {fmtDistanceKm(metrics.eq.drivingKm, units)} of driving
                </Text> : null}
                {metrics.eq.phoneCharges ? <Text style={[styles.eqText, { color: colors.text }]}>
                  • ≈ charging {metrics.eq.phoneCharges} phones
                </Text> : null}
              </View>
            ) : null}
            <Text style={[styles.footnote, { color: colors.text + '99' }]}>Est. Estimates vary by region. <Text style={[styles.link, { color: '#10b981' }]}>Learn how we calculate</Text></Text>
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  backdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'flex-end' },
  sheet: { borderTopLeftRadius: 20, borderTopRightRadius: 20, padding: 16, maxHeight: '80%' },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  title: { fontSize: 18, fontWeight: '700' },
  stat: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 12, borderBottomWidth: 1 },
  statLabel: { fontSize: 14 },
  statValue: { fontSize: 16, fontWeight: '700' },
  eq: { marginTop: 16, padding: 12, borderRadius: 8 },
  eqTitle: { fontSize: 14, fontWeight: '700', marginBottom: 6 },
  eqText: { fontSize: 13, marginTop: 4 },
  footnote: { fontSize: 12, marginTop: 16, textAlign: 'center' },
  link: { fontWeight: '600' },
});
