import { TouchableOpacity, Text, StyleSheet, Alert } from 'react-native';
import { useTheme } from '@react-navigation/native';

type Props = { onSignOut?: () => void };

export default function SignOutRow({ onSignOut }: Props) {
  const { colors } = useTheme();
  const handleSignOut = () => {
    Alert.alert('Sign out', "You'll keep your data on this device.", [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Sign out', style: 'destructive', onPress: onSignOut },
    ]);
  };

  return (
    <TouchableOpacity style={[styles.btn, { backgroundColor: colors.card, borderColor: '#dc2626' }]} onPress={handleSignOut}>
      <Text style={[styles.btnText, { color: '#dc2626' }]}>Sign out</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  btn: { borderRadius: 12, paddingVertical: 14, alignItems: 'center', shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 10, elevation: 4, borderWidth: 1 },
  btnText: { fontSize: 16, fontWeight: '600' },
});

