import { View, Text, StyleSheet, Modal, TouchableOpacity, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

type Props = { visible: boolean; onClose: () => void; metrics: { items: number; co2eKg: number; waterL?: number; energyKwh?: number; eq?: { drivingKm?: number; phoneCharges?: number } } };

export default function ImpactBreakdownSheet({ visible, onClose, metrics }: Props) {
  return (
    <Modal visible={visible} animationType="slide" transparent onRequestClose={onClose}>
      <View style={styles.backdrop}>
        <View style={styles.sheet}>
          <View style={styles.header}>
            <Text style={styles.title}>Your Impact Breakdown</Text>
            <TouchableOpacity onPress={onClose}>
              <Ionicons name="close" size={24} color="#333" />
            </TouchableOpacity>
          </View>
          <ScrollView>
            <View style={styles.stat}>
              <Text style={styles.statLabel}>Items sorted</Text>
              <Text style={styles.statValue}>{metrics.items}</Text>
            </View>
            <View style={styles.stat}>
              <Text style={styles.statLabel}>CO₂e avoided</Text>
              <Text style={styles.statValue}>{metrics.co2eKg.toFixed(1)} kg</Text>
            </View>
            {metrics.waterL ? (
              <View style={styles.stat}>
                <Text style={styles.statLabel}>Water saved</Text>
                <Text style={styles.statValue}>{metrics.waterL} L</Text>
              </View>
            ) : null}
            {metrics.energyKwh ? (
              <View style={styles.stat}>
                <Text style={styles.statLabel}>Energy saved</Text>
                <Text style={styles.statValue}>{metrics.energyKwh.toFixed(1)} kWh</Text>
              </View>
            ) : null}
            {metrics.eq ? (
              <View style={styles.eq}>
                <Text style={styles.eqTitle}>Equivalents</Text>
                {metrics.eq.drivingKm ? <Text style={styles.eqText}>• {metrics.co2eKg.toFixed(1)} kg CO₂e ≈ {metrics.eq.drivingKm} km of driving</Text> : null}
                {metrics.eq.phoneCharges ? <Text style={styles.eqText}>• ≈ charging {metrics.eq.phoneCharges} phones</Text> : null}
              </View>
            ) : null}
            <Text style={styles.footnote}>Est. Estimates vary by region. <Text style={styles.link}>Learn how we calculate</Text></Text>
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'flex-end' },
  sheet: { backgroundColor: '#fff', borderTopLeftRadius: 20, borderTopRightRadius: 20, padding: 16, maxHeight: '80%' },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  title: { fontSize: 18, fontWeight: '700' },
  stat: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: '#f0f0f0' },
  statLabel: { fontSize: 14, color: '#666' },
  statValue: { fontSize: 16, fontWeight: '700', color: '#333' },
  eq: { marginTop: 16, padding: 12, backgroundColor: '#f9f9f9', borderRadius: 8 },
  eqTitle: { fontSize: 14, fontWeight: '700', marginBottom: 6 },
  eqText: { fontSize: 13, color: '#555', marginTop: 4 },
  footnote: { fontSize: 12, color: '#999', marginTop: 16, textAlign: 'center' },
  link: { color: '#10b981', fontWeight: '600' },
});

