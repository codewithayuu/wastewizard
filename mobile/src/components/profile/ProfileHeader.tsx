import { View, Text, StyleSheet, TouchableOpacity, Image } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { User } from '../../types/profile';

type Props = { user: User; onShare?: () => void; onSettings?: () => void; onGoogleSignIn?: () => void };

export default function ProfileHeader({ user, onShare, onSettings, onGoogleSignIn }: Props) {
  const subtitles = ['Keep up the streak!', 'Sorting hero in the making', 'Every item counts.'];
  const subtitle = subtitles[Math.floor(Math.random() * subtitles.length)];

  return (
    <View style={styles.card}>
      <View style={styles.row}>
        <TouchableOpacity style={styles.avatar}>
          {user.photoURL ? (
            <Image source={{ uri: user.photoURL }} style={styles.avatarImg} />
          ) : (
            <View style={styles.avatarPlaceholder}>
              <Ionicons name="person" size={32} color="#10b981" />
            </View>
          )}
        </TouchableOpacity>
        <View style={styles.center}>
          <Text style={styles.greeting}>Hi, {user.name} 👋</Text>
          <Text style={styles.subtitle}>{subtitle}</Text>
        </View>
        <View style={styles.actions}>
          <TouchableOpacity onPress={onShare} style={styles.iconBtn}>
            <Ionicons name="arrow-up-outline" size={22} color="#333" />
          </TouchableOpacity>
          <TouchableOpacity onPress={onSettings} style={styles.iconBtn}>
            <Ionicons name="settings-outline" size={22} color="#333" />
          </TouchableOpacity>
        </View>
      </View>

      <View style={styles.statsRow}>
        <View style={styles.chip}>
          <Text style={styles.chipText}>{user.points} pts</Text>
        </View>
        <View style={styles.chip}>
          <Text style={styles.chipText}>{user.streakDays}‑day streak</Text>
        </View>
        <View style={styles.chip}>
          <Text style={styles.chipText}>Level: {user.level}</Text>
        </View>
      </View>

      {user.isGuest && (
        <View style={styles.guestBox}>
          <Text style={styles.guestText}>Sync your points and activity across devices.</Text>
          <TouchableOpacity onPress={onGoogleSignIn} style={styles.googleBtn}>
            <Ionicons name="logo-google" size={18} color="#111827" />
            <Text style={styles.googleBtnText}>Continue with Google</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: '#fff', borderRadius: 16, padding: 16, shadowColor: '#000', shadowOpacity: 0.08, shadowRadius: 10, elevation: 4 },
  row: { flexDirection: 'row', alignItems: 'center', marginBottom: 12 },
  avatar: { width: 64, height: 64, borderRadius: 32, marginRight: 12 },
  avatarImg: { width: 64, height: 64, borderRadius: 32 },
  avatarPlaceholder: { width: 64, height: 64, borderRadius: 32, backgroundColor: '#e0f2f1', alignItems: 'center', justifyContent: 'center' },
  center: { flex: 1 },
  greeting: { fontSize: 20, fontWeight: '700', marginBottom: 2 },
  subtitle: { fontSize: 14, color: '#666' },
  actions: { flexDirection: 'row', gap: 8 },
  iconBtn: { width: 36, height: 36, borderRadius: 18, backgroundColor: '#f3f4f6', alignItems: 'center', justifyContent: 'center' },
  statsRow: { flexDirection: 'row', gap: 8, flexWrap: 'wrap' },
  chip: { paddingHorizontal: 12, paddingVertical: 6, borderRadius: 999, backgroundColor: '#e0f2f1' },
  chipText: { fontSize: 13, fontWeight: '600', color: '#00695c' },
  guestBox: { marginTop: 12, paddingTop: 12, borderTopWidth: 1, borderTopColor: '#e5e7eb' },
  guestText: { fontSize: 13, color: '#666', marginBottom: 10, textAlign: 'center' },
  googleBtn: { height: 50, borderRadius: 12, backgroundColor: '#fff', borderWidth: 1, borderColor: '#e6e6e6', flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 10 },
  googleBtnText: { fontSize: 16, color: '#111827', fontWeight: '600' },
});

