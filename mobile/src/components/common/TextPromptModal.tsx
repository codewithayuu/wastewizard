import React, { useState, useEffect } from 'react';
import { Modal, View, Text, StyleSheet, TextInput, TouchableOpacity } from 'react-native';
import { useTheme } from '@react-navigation/native';

type Props = {
  visible: boolean;
  title: string;
  placeholder?: string;
  initialValue?: string;
  confirmLabel?: string;
  onCancel: () => void;
  onSubmit: (value: string) => void;
};

export default function TextPromptModal({ visible, title, placeholder, initialValue = '', confirmLabel = 'Save', onCancel, onSubmit }: Props) {
  const { colors } = useTheme();
  const [value, setValue] = useState(initialValue);
  useEffect(() => setValue(initialValue), [initialValue, visible]);

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onCancel}>
      <View style={styles.backdrop}>
        <View style={[styles.card, { backgroundColor: colors.card }] }>
          <Text style={[styles.title, { color: colors.text }]}>{title}</Text>
          <TextInput
            value={value}
            onChangeText={setValue}
            placeholder={placeholder}
            placeholderTextColor={colors.text + '66'}
            style={[styles.input, { color: colors.text, borderColor: colors.border }]}
            autoFocus
          />
          <View style={styles.actions}>
            <TouchableOpacity onPress={onCancel} style={[styles.btn, { borderColor: colors.border }]}>
              <Text style={[styles.btnText, { color: colors.text }]}>Cancel</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => onSubmit(value)} style={[styles.btn, { backgroundColor: colors.primary }]}>
              <Text style={[styles.btnText, { color: '#fff' }]}>{confirmLabel}</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.45)', alignItems: 'center', justifyContent: 'center', padding: 20 },
  card: { width: '100%', maxWidth: 420, borderRadius: 14, padding: 16 },
  title: { fontSize: 16, fontWeight: '700', marginBottom: 8 },
  input: { borderWidth: 1, borderRadius: 10, paddingHorizontal: 12, paddingVertical: 10, marginTop: 4 },
  actions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 10, marginTop: 12 },
  btn: { paddingHorizontal: 14, paddingVertical: 10, borderRadius: 10, borderWidth: 1 },
  btnText: { fontWeight: '700' },
});
