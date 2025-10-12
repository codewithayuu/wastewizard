import { useEffect, useMemo, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Linking, Switch, TextInput } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme } from '@react-navigation/native';
import { useAppStore } from '../store/appStore';
import { FACTS } from './facts';
import { getStoredApiBaseUrl, setStoredApiBaseUrl } from '../storage';

type Step = 1 | 2 | 3 | 4 | 5 | 6 | 7;

export default function Onboarding({ onDone }: { onDone: () => void }) {
  const { colors } = useTheme();
  const setUser = useAppStore((s) => s.setUser);
  const [step, setStep] = useState<Step>(1);
  const [showFactOnStartup, setShowFactOnStartup] = useState(false);
  const [name, setName] = useState('');

  const randomFact = useMemo(() => FACTS[Math.floor(Math.random() * FACTS.length)], []);

  useEffect(() => {
    // Could prefetch stored settings here if needed
  }, []);

  const next = () => setStep((s) => (s < 7 ? ((s + 1) as Step) : s));
  const skip = () => setStep(7);

  return (
    <LinearGradient colors={[colors.background, colors.background]} start={{ x: 0, y: 0 }} end={{ x: 1, y: 1 }} style={styles.flex}>
      <View style={[styles.container, { backgroundColor: colors.background }]}>
        {step === 1 && (
          <View style={styles.center}>
            <Text style={styles.brand}>Waste Wizard</Text>
            <Text style={styles.tag}>Sort smarter. Waste less.</Text>
            <View style={[styles.row, styles.centerRow]}>
              <TouchableOpacity style={styles.secondary} onPress={skip}><Text>Skip</Text></TouchableOpacity>
              <TouchableOpacity style={styles.primary} onPress={next}><Text style={styles.primaryText}>Get started</Text></TouchableOpacity>
            </View>
          </View>
        )}
        {step === 2 && (
          <View style={[styles.card, styles.centeredCard, { backgroundColor: colors.card }]}>
            <Text style={[styles.h1, { color: colors.text }]}>Why we built Waste Wizard</Text>
            <Text style={[styles.body, { color: colors.text }]}>• Sorting can be confusing.{"\n"}
• Point your camera—get simple guidance.{"\n"}
• Every correct sort reduces contamination and saves resources.</Text>
            <View style={[styles.row, styles.centerRow]}>
              <TouchableOpacity style={styles.secondary} onPress={skip}><Text>Skip</Text></TouchableOpacity>
              <TouchableOpacity style={styles.primary} onPress={next}><Text style={styles.primaryText}>Show me how</Text></TouchableOpacity>
            </View>
          </View>
        )}

        {step === 3 && (
          <View style={[styles.card, styles.centeredCard, { backgroundColor: colors.card }]}>
            <Text style={styles.chip}>Did you know? 💡</Text>
            {randomFact.stat ? <Text style={[styles.stat, { color: colors.text }]}>{randomFact.stat}</Text> : null}
            <Text style={[styles.body, { color: colors.text }]}>{randomFact.body}</Text>
            {randomFact.source ? (
              <Text style={styles.source} onPress={() => Linking.openURL('https://www.epa.gov/recycle')}>Source: {randomFact.source}</Text>
            ) : null}
            <View style={[styles.row, styles.centerRow, { alignItems: 'center' }]}>
              <TouchableOpacity style={styles.secondary} onPress={next}><Text>Next fact</Text></TouchableOpacity>
              <View style={{ flexDirection: 'row', alignItems: 'center' }}>
                <Text style={{ marginRight: 6, color: colors.text }}>Show at startup</Text>
                <Switch value={showFactOnStartup} onValueChange={setShowFactOnStartup} />
              </View>
              <TouchableOpacity style={styles.primary} onPress={next}><Text style={styles.primaryText}>Continue</Text></TouchableOpacity>
            </View>
          </View>
        )}

        {step === 4 && (
          <View style={[styles.card, styles.centeredCard, { backgroundColor: colors.card }]}>
            <Text style={[styles.h1, { color: colors.text }]}>How it helps</Text>
            <Text style={[styles.body, { color: colors.text }]}>Scan → Learn → Act{"\n"}
Scan: Point your camera or barcode{"\n"}
Learn: Get simple sorting guidance{"\n"}
Act: Track your impact and build better habits</Text>
            <View style={[styles.row, styles.centerRow]}>
              <TouchableOpacity style={styles.secondary} onPress={skip}><Text>Skip for now</Text></TouchableOpacity>
              <TouchableOpacity style={styles.primary} onPress={next}><Text style={styles.primaryText}>Personalize</Text></TouchableOpacity>
            </View>
          </View>
        )}

        {step === 5 && (
          <View style={[styles.card, styles.centeredCard, { backgroundColor: colors.card }]}>
            <Text style={[styles.h1, { color: colors.text }]}>Permissions</Text>
            <Text style={[styles.body, { color: colors.text }]}>Camera: only used when you scan.{"\n"}
Notifications (optional): reminders and achievements.</Text>
            <View style={[styles.row, styles.centerRow]}>
              <TouchableOpacity style={styles.secondary} onPress={skip}><Text>Not now</Text></TouchableOpacity>
              <TouchableOpacity style={styles.primary} onPress={next}><Text style={styles.primaryText}>Continue</Text></TouchableOpacity>
            </View>
          </View>
        )}

        {step === 6 && (
          <View style={[styles.card, styles.centeredCard, { backgroundColor: colors.card }]}>
            <Text style={[styles.h1, { color: colors.text }]}>What should we call you?</Text>
            <TextInput
              value={name}
              onChangeText={setName}
              placeholder="Your name"
              placeholderTextColor={colors.text + '66'}
              style={[styles.input, { color: colors.text, borderColor: colors.border }]}
            />
            <View style={[styles.row, styles.centerRow]}>
              <TouchableOpacity style={styles.secondary} onPress={next}><Text>Skip</Text></TouchableOpacity>
              <TouchableOpacity style={styles.primary} onPress={() => { setUser({ name: name || 'Guest', isGuest: false }); next(); }}><Text style={styles.primaryText}>Save</Text></TouchableOpacity>
            </View>
          </View>
        )}

        {step === 7 && (
          <View style={styles.center}>
            <Text style={[styles.h1, { color: colors.text }]}>You’re set!</Text>
            <Text style={[styles.body, { color: colors.text }]}>Start with a quick scan or browse the guide.</Text>
            <TouchableOpacity style={styles.primary} onPress={onDone}><Text style={styles.primaryText}>Open Scanner</Text></TouchableOpacity>
          </View>
        )}
      </View>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  container: { flex: 1, padding: 20, justifyContent: 'center' },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 20 },
  brand: { fontSize: 28, fontWeight: '800' },
  tag: { marginTop: 8, fontSize: 16 },
  h1: { fontSize: 22, fontWeight: '700', marginBottom: 8 },
  body: { fontSize: 16 },
  chip: { alignSelf: 'flex-start', backgroundColor: '#e0f2f1', color: '#00695c', paddingHorizontal: 10, paddingVertical: 4, borderRadius: 999, marginBottom: 8 },
  stat: { fontSize: 40, fontWeight: '800' },
  source: { marginTop: 6, color: '#1976d2' },
  row: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 16 },
  centerRow: { justifyContent: 'center', gap: 12 },
  card: { borderRadius: 16, padding: 16, shadowColor: '#000', shadowOpacity: 0.1, shadowRadius: 8 },
  centeredCard: { alignSelf: 'center', width: '100%', maxWidth: 520 },
  primary: { backgroundColor: '#2e7d32', paddingHorizontal: 14, paddingVertical: 10, borderRadius: 10 },
  primaryText: { color: 'white', fontWeight: '700' },
  secondary: { paddingHorizontal: 14, paddingVertical: 10 },
  input: { borderWidth: 1, borderRadius: 10, paddingHorizontal: 12, paddingVertical: 10, marginTop: 8 },
});


