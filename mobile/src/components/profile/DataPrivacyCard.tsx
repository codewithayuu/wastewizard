import { View, Text, StyleSheet, TouchableOpacity, Alert } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

type Props = { isGuest: boolean; onManageAccount?: () => void; onExport?: () => void; onClearCache?: () => void; onDelete?: () => void };

export default function DataPrivacyCard({ isGuest, onManageAccount, onExport, onClearCache, onDelete }: Props) {
  const handleClearCache = () => {
    Alert.alert('Clear cache', 'This will remove cached images and temporary files.', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Clear', onPress: onClearCache },
    ]);
  };

  const handleDelete = () => {
    Alert.alert('Delete account', 'This action cannot be undone. All your data will be deleted permanently.', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: onDelete },
    ]);
  };

  return (
    <View style={styles.card}>
      <Text style={styles.title}>Data & Privacy</Text>

      <TouchableOpacity style={styles.row} onPress={onManageAccount}>
        <Ionicons name="person-circle-outline" size={20} color="#666" />
        <Text style={styles.rowText}>Manage account</Text>
        <Ionicons name="chevron-forward" size={18} color="#999" />
      </TouchableOpacity>

      <TouchableOpacity style={styles.row} onPress={onExport}>
        <Ionicons name="download-outline" size={20} color="#666" />
        <Text style={styles.rowText}>Export activity (CSV)</Text>
        <Ionicons name="chevron-forward" size={18} color="#999" />
      </TouchableOpacity>

      <TouchableOpacity style={styles.row} onPress={handleClearCache}>
        <Ionicons name="trash-outline" size={20} color="#666" />
        <Text style={styles.rowText}>Clear local cache</Text>
      </TouchableOpacity>

      <TouchableOpacity style={[styles.row, styles.rowDanger]} onPress={handleDelete}>
        <Ionicons name="warning-outline" size={20} color="#dc2626" />
        <Text style={[styles.rowText, styles.rowTextDanger]}>Delete account</Text>
      </TouchableOpacity>

      {isGuest && (
        <View style={styles.nudge}>
          <Text style={styles.nudgeText}>Link Google to back up your data.</Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: '#fff', borderRadius: 16, padding: 16, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 10, elevation: 4 },
  title: { fontSize: 18, fontWeight: '700', marginBottom: 12 },
  row: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: '#e5e7eb', gap: 10 },
  rowText: { flex: 1, fontSize: 15 },
  rowDanger: { borderBottomWidth: 0 },
  rowTextDanger: { color: '#dc2626' },
  nudge: { marginTop: 12, padding: 10, backgroundColor: '#fef3c7', borderRadius: 8 },
  nudgeText: { fontSize: 13, color: '#92400e', textAlign: 'center' },
});

