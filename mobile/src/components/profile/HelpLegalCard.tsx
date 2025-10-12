import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@react-navigation/native';

type Props = { onFAQ?: () => void; onContact?: () => void; onReport?: () => void; onTerms?: () => void; onPrivacy?: () => void };

export default function HelpLegalCard({ onFAQ, onContact, onReport, onTerms, onPrivacy }: Props) {
  const { colors } = useTheme();
  return (
    <View style={[styles.card, { backgroundColor: colors.card }] }>
      <Text style={[styles.title, { color: colors.text }]}>Help & Legal</Text>

      <TouchableOpacity style={[styles.row, { borderBottomColor: colors.border }]} onPress={onFAQ}>
        <Ionicons name="help-circle-outline" size={20} color={colors.text + '99'} />
        <Text style={[styles.rowText, { color: colors.text }]}>FAQs</Text>
        <Ionicons name="chevron-forward" size={18} color={colors.text + '66'} />
      </TouchableOpacity>

      <TouchableOpacity style={[styles.row, { borderBottomColor: colors.border }]} onPress={onContact}>
        <Ionicons name="mail-outline" size={20} color={colors.text + '99'} />
        <Text style={[styles.rowText, { color: colors.text }]}>Contact support</Text>
        <Ionicons name="chevron-forward" size={18} color={colors.text + '66'} />
      </TouchableOpacity>

      <TouchableOpacity style={[styles.row, { borderBottomColor: colors.border }]} onPress={onReport}>
        <Ionicons name="flag-outline" size={20} color={colors.text + '99'} />
        <Text style={[styles.rowText, { color: colors.text }]}>Report an issue</Text>
        <Ionicons name="chevron-forward" size={18} color={colors.text + '66'} />
      </TouchableOpacity>

      <TouchableOpacity style={[styles.row, { borderBottomColor: colors.border }]} onPress={onTerms}>
        <Ionicons name="document-text-outline" size={20} color={colors.text + '99'} />
        <Text style={[styles.rowText, { color: colors.text }]}>Terms of Service</Text>
        <Ionicons name="chevron-forward" size={18} color={colors.text + '66'} />
      </TouchableOpacity>

      <TouchableOpacity style={[styles.row, { borderBottomWidth: 0, borderBottomColor: colors.border }]} onPress={onPrivacy}>
        <Ionicons name="shield-checkmark-outline" size={20} color={colors.text + '99'} />
        <Text style={[styles.rowText, { color: colors.text }]}>Privacy Policy</Text>
        <Ionicons name="chevron-forward" size={18} color={colors.text + '66'} />
      </TouchableOpacity>

      <View style={[styles.about, { borderTopColor: colors.border }]}>
        <Text style={[styles.aboutText, { color: colors.text + '66' }]}>Version 1.0.0 (build 1)</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { borderRadius: 16, padding: 16, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 10, elevation: 4 },
  title: { fontSize: 18, fontWeight: '700', marginBottom: 12 },
  row: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, borderBottomWidth: 1, gap: 10 },
  rowText: { flex: 1, fontSize: 15 },
  about: { marginTop: 12, paddingTop: 12, borderTopWidth: 1 },
  aboutText: { fontSize: 12, textAlign: 'center' },
});

