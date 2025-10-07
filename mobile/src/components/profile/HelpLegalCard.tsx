import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

type Props = { onFAQ?: () => void; onContact?: () => void; onReport?: () => void; onTerms?: () => void; onPrivacy?: () => void };

export default function HelpLegalCard({ onFAQ, onContact, onReport, onTerms, onPrivacy }: Props) {
  return (
    <View style={styles.card}>
      <Text style={styles.title}>Help & Legal</Text>

      <TouchableOpacity style={styles.row} onPress={onFAQ}>
        <Ionicons name="help-circle-outline" size={20} color="#666" />
        <Text style={styles.rowText}>FAQs</Text>
        <Ionicons name="chevron-forward" size={18} color="#999" />
      </TouchableOpacity>

      <TouchableOpacity style={styles.row} onPress={onContact}>
        <Ionicons name="mail-outline" size={20} color="#666" />
        <Text style={styles.rowText}>Contact support</Text>
        <Ionicons name="chevron-forward" size={18} color="#999" />
      </TouchableOpacity>

      <TouchableOpacity style={styles.row} onPress={onReport}>
        <Ionicons name="flag-outline" size={20} color="#666" />
        <Text style={styles.rowText}>Report an issue</Text>
        <Ionicons name="chevron-forward" size={18} color="#999" />
      </TouchableOpacity>

      <TouchableOpacity style={styles.row} onPress={onTerms}>
        <Ionicons name="document-text-outline" size={20} color="#666" />
        <Text style={styles.rowText}>Terms of Service</Text>
        <Ionicons name="chevron-forward" size={18} color="#999" />
      </TouchableOpacity>

      <TouchableOpacity style={[styles.row, { borderBottomWidth: 0 }]} onPress={onPrivacy}>
        <Ionicons name="shield-checkmark-outline" size={20} color="#666" />
        <Text style={styles.rowText}>Privacy Policy</Text>
        <Ionicons name="chevron-forward" size={18} color="#999" />
      </TouchableOpacity>

      <View style={styles.about}>
        <Text style={styles.aboutText}>Version 1.0.0 (build 1)</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: '#fff', borderRadius: 16, padding: 16, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 10, elevation: 4 },
  title: { fontSize: 18, fontWeight: '700', marginBottom: 12 },
  row: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: '#e5e7eb', gap: 10 },
  rowText: { flex: 1, fontSize: 15 },
  about: { marginTop: 12, paddingTop: 12, borderTopWidth: 1, borderTopColor: '#e5e7eb' },
  aboutText: { fontSize: 12, color: '#999', textAlign: 'center' },
});

