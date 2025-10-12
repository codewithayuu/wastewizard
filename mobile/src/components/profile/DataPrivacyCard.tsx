import { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme } from '@react-navigation/native';
import ConfirmModal from '../common/ConfirmModal';

type Props = { isGuest: boolean; onManageAccount?: () => void; onExport?: () => void; onClearCache?: () => void; onDelete?: () => void };

export default function DataPrivacyCard({ isGuest, onManageAccount, onExport, onClearCache, onDelete }: Props) {
  const { colors } = useTheme();
  const [confirmClear, setConfirmClear] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  return (
    <View style={[styles.card, { backgroundColor: colors.card }] }>
      <Text style={[styles.title, { color: colors.text }]}>Data & Privacy</Text>

      <TouchableOpacity style={[styles.row, { borderBottomColor: colors.border }]} onPress={onManageAccount}>
        <Ionicons name="person-circle-outline" size={20} color="#666" />
        <Text style={[styles.rowText, { color: colors.text }]}>Manage account</Text>
        <Ionicons name="chevron-forward" size={18} color="#999" />
      </TouchableOpacity>

      <TouchableOpacity style={[styles.row, { borderBottomColor: colors.border }]} onPress={onExport}>
        <Ionicons name="download-outline" size={20} color="#666" />
        <Text style={[styles.rowText, { color: colors.text }]}>Export activity (CSV)</Text>
        <Ionicons name="chevron-forward" size={18} color="#999" />
      </TouchableOpacity>

      <TouchableOpacity style={[styles.row, { borderBottomColor: colors.border }]} onPress={() => setConfirmClear(true)}>
        <Ionicons name="trash-outline" size={20} color="#666" />
        <Text style={[styles.rowText, { color: colors.text }]}>Clear local cache</Text>
      </TouchableOpacity>

      <TouchableOpacity style={[styles.row, styles.rowDanger]} onPress={() => setConfirmDelete(true)}>
        <Ionicons name="warning-outline" size={20} color="#dc2626" />
        <Text style={[styles.rowText, styles.rowTextDanger]}>Delete account</Text>
      </TouchableOpacity>

      {isGuest && (
        <View style={[styles.nudge, { backgroundColor: '#fef3c7' }] }>
          <Text style={styles.nudgeText}>Link Google to back up your data.</Text>
        </View>
      )}

      <ConfirmModal
        visible={confirmClear}
        title="Clear cache"
        message="This will remove cached images and temporary files."
        confirmLabel="Clear"
        onCancel={() => setConfirmClear(false)}
        onConfirm={() => { setConfirmClear(false); onClearCache?.(); }}
      />
      <ConfirmModal
        visible={confirmDelete}
        title="Delete account"
        message="This action cannot be undone. All your data will be deleted permanently."
        confirmLabel="Delete"
        destructive
        onCancel={() => setConfirmDelete(false)}
        onConfirm={() => { setConfirmDelete(false); onDelete?.(); }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  card: { borderRadius: 16, padding: 16, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 10, elevation: 4 },
  title: { fontSize: 18, fontWeight: '700', marginBottom: 12 },
  row: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, borderBottomWidth: 1, gap: 10 },
  rowText: { flex: 1, fontSize: 15 },
  rowDanger: { borderBottomWidth: 0 },
  rowTextDanger: { color: '#dc2626' },
  nudge: { marginTop: 12, padding: 10, backgroundColor: '#fef3c7', borderRadius: 8 },
  nudgeText: { fontSize: 13, color: '#92400e', textAlign: 'center' },
});

