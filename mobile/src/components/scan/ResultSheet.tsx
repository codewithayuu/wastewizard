import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

type Result = { item: string; category: string; confidence: number; co2eKg: number; instructions: string; prep: string[] };

type Props = { result: Result; onAddToLog: () => void; onRetake: () => void; onClose: () => void };

export default function ResultSheet({ result, onAddToLog, onRetake, onClose }: Props) {
  return (
    <View style={styles.container}>
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        <View style={styles.header}>
          <View>
            <Text style={styles.title}>{result.item}</Text>
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{result.category}</Text>
            </View>
          </View>
          <TouchableOpacity onPress={onClose}>
            <Ionicons name="close" size={28} color="#333" />
          </TouchableOpacity>
        </View>

        <View style={styles.confidence}>
          <Text style={styles.confText}>{Math.round(result.confidence * 100)}% confidence</Text>
          <TouchableOpacity><Text style={styles.link}>Why this guess?</Text></TouchableOpacity>
        </View>

        <View style={styles.impact}>
          <Ionicons name="leaf-outline" size={20} color="#10b981" />
          <Text style={styles.impactText}>Avoids ~{result.co2eKg.toFixed(2)} kg CO₂e (Est.)</Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Disposal instructions</Text>
          <Text style={styles.body}>{result.instructions}</Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Preparation</Text>
          {result.prep.map((p, i) => <Text key={i} style={styles.bullet}>• {p}</Text>)}
        </View>

        <View style={styles.actions}>
          <TouchableOpacity style={styles.primary} onPress={onAddToLog}>
            <Text style={styles.primaryText}>Add to log (+10 pts)</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.secondary} onPress={onRetake}>
            <Text style={styles.secondaryText}>Retake</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.footer}>
          <TouchableOpacity><Text style={styles.link}>Report incorrect</Text></TouchableOpacity>
          <TouchableOpacity><Text style={styles.link}>View in Guide</Text></TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, borderTopLeftRadius: 20, borderTopRightRadius: 20, overflow: 'hidden' },
  content: { padding: 16 },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 },
  title: { fontSize: 20, fontWeight: '700', marginBottom: 4 },
  badge: { alignSelf: 'flex-start', backgroundColor: '#e0f2f1', paddingHorizontal: 10, paddingVertical: 4, borderRadius: 999 },
  badgeText: { fontSize: 12, fontWeight: '600', color: '#00695c' },
  confidence: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 },
  confText: { fontSize: 14, color: '#666' },
  link: { fontSize: 14, color: '#10b981', fontWeight: '600' },
  impact: { flexDirection: 'row', alignItems: 'center', gap: 6, backgroundColor: '#f0fdf4', padding: 10, borderRadius: 8, marginBottom: 12 },
  impactText: { fontSize: 14, color: '#166534', fontWeight: '600' },
  section: { marginBottom: 16 },
  sectionTitle: { fontSize: 16, fontWeight: '700', marginBottom: 6 },
  body: { fontSize: 14, color: '#333' },
  bullet: { fontSize: 14, color: '#555', marginTop: 4 },
  actions: { flexDirection: 'row', gap: 8, marginBottom: 12 },
  primary: { flex: 1, backgroundColor: '#10b981', paddingVertical: 12, borderRadius: 10, alignItems: 'center' },
  primaryText: { color: '#fff', fontWeight: '700' },
  secondary: { flex: 1, borderWidth: 1, borderColor: '#10b981', paddingVertical: 12, borderRadius: 10, alignItems: 'center' },
  secondaryText: { color: '#10b981', fontWeight: '600' },
  footer: { flexDirection: 'row', justifyContent: 'space-around', paddingTop: 8, borderTopWidth: 1, borderTopColor: '#e0e0e0' },
});

