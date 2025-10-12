import React from 'react';
import { Modal, View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { useTheme } from '@react-navigation/native';

type Props = {
  visible: boolean;
  title: string;
  message?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  destructive?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
};

export default function ConfirmModal({ visible, title, message, confirmLabel = 'Confirm', cancelLabel = 'Cancel', destructive, onConfirm, onCancel }: Props) {
  const { colors, dark } = useTheme();
  return (
    <Modal visible={visible} animationType="fade" transparent onRequestClose={onCancel}>
      <View style={styles.backdrop}>
        <View style={[styles.card, { backgroundColor: colors.card }]}> 
          <Text style={[styles.title, { color: colors.text }]}>{title}</Text>
          {message ? <Text style={[styles.msg, { color: colors.text }]}>{message}</Text> : null}
          <View style={styles.row}>
            <TouchableOpacity onPress={onCancel} style={[styles.btn, { borderColor: colors.border }]}>
              <Text style={[styles.btnText, { color: colors.text }]}>{cancelLabel}</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={onConfirm} style={[styles.btn, { backgroundColor: destructive ? '#ef4444' : colors.primary, borderColor: 'transparent' }]}>
              <Text style={[styles.btnText, { color: '#fff' }]}>{confirmLabel}</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', alignItems: 'center', justifyContent: 'center', padding: 24 },
  card: { width: '100%', maxWidth: 480, borderRadius: 16, padding: 16 },
  title: { fontSize: 18, fontWeight: '700', marginBottom: 8 },
  msg: { fontSize: 14, opacity: 0.85 },
  row: { flexDirection: 'row', justifyContent: 'flex-end', gap: 10, marginTop: 16 },
  btn: { paddingHorizontal: 14, paddingVertical: 10, borderRadius: 10, borderWidth: 1 },
  btnText: { fontSize: 14, fontWeight: '600' },
});
