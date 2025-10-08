import { StatusBar } from 'expo-status-bar';
import { useEffect, useState } from 'react';
import { StyleSheet, View, useColorScheme } from 'react-native';
import Onboarding from './src/onboarding/Onboarding';
import HomeScreen from './src/screens/HomeScreen';
import ProfileScreen from './src/screens/ProfileScreen';
import ScanScreen from './src/screens/ScanScreen';
import { NavigationContainer, useTheme } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { BlurView } from 'expo-blur';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Pressable, Text, Platform } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useAppStore } from './src/store/appStore';
import { LightTheme, DarkTheme_ } from './src/theme/theme';
import { GestureHandlerRootView } from 'react-native-gesture-handler';

const Tab = createBottomTabNavigator();
const Stack = createNativeStackNavigator();

export default function App() {
  const [firstOpen, setFirstOpen] = useState<boolean | null>(null);
  const scheme = useColorScheme();
  const themePref = useAppStore((s) => s.settings.theme);
  const isDark = themePref === 'system' ? scheme === 'dark' : themePref === 'dark';

  useEffect(() => {
    (async () => {
      const v = await AsyncStorage.getItem('onboardingDone');
      setFirstOpen(v ? false : true);
    })();
  }, []);

  if (firstOpen === null) {
    return <View style={{ flex: 1 }} />;
  }

  function HomeStack() {
    return (
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        <Stack.Screen name="HomeMain" component={HomeScreen} />
      </Stack.Navigator>
    );
  }

  function ProfileStack() {
    return (
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        <Stack.Screen name="ProfileMain" component={ProfileScreen} />
      </Stack.Navigator>
    );
  }

  function FloatingTabBar({ state, descriptors, navigation }: any) {
    const { bottom } = useSafeAreaInsets();
    const { colors, dark } = useTheme();
    const bg = dark ? 'rgba(17,24,39,0.80)' : 'rgba(255,255,255,0.92)';
    const border = dark ? 'rgba(255,255,255,0.08)' : 'rgba(17,24,39,0.08)';
    const inactive = dark ? 'rgba(229,231,235,0.7)' : 'rgba(17,24,39,0.7)';
    const active = colors.primary;
    const goScan = () => navigation.navigate('ScanModal');
    return (
      <View pointerEvents="box-none" style={{ position: 'absolute', left: 0, right: 0, bottom: 0 }}>
        <View
          style={{
            position: 'absolute', left: 16, right: 16, bottom: (bottom || 16) + 12, height: 68, borderRadius: 34,
            backgroundColor: bg, borderWidth: 1, borderColor: border,
            overflow: 'hidden', shadowColor: '#000', shadowOpacity: 0.18, shadowRadius: 16, shadowOffset: { width: 0, height: 8 }, elevation: 14,
          }}
        >
          {Platform.OS === 'ios' ? (
            <BlurView intensity={24} tint={dark ? 'dark' : 'light'} style={StyleSheet.absoluteFill} />
          ) : null}
          <View style={{ position: 'absolute', left: 0, right: 0, top: 0, bottom: 0, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 24 }}>
            {['Home', 'Profile'].map((routeName) => {
              const routeIndex = state.routes.findIndex((r: any) => r.name === routeName);
              const isFocused = state.index === routeIndex;
              const onPress = () => {
                const event = navigation.emit({ type: 'tabPress', target: state.routes[routeIndex].key, canPreventDefault: true });
                if (!isFocused && !event.defaultPrevented) navigation.navigate(routeName);
                else navigation.emit({ type: 'tabLongPress', target: state.routes[routeIndex].key });
              };
              const iconName = routeName === 'Home' ? 'home' : 'person';
              return (
                <Pressable key={routeName} onPress={onPress} style={{ alignItems: 'center', justifyContent: 'center', minWidth: 80 }}>
                  <Ionicons name={iconName as any} size={22} color={isFocused ? active : inactive} />
                  {isFocused ? <Text style={{ fontSize: 12, color: active, marginTop: 4 }}>{routeName}</Text> : null}
                </Pressable>
              );
            })}
          </View>
        </View>

        <Pressable
          onPress={goScan}
          style={{ position: 'absolute', alignSelf: 'center', bottom: (bottom || 16) + 32, width: 64, height: 64, borderRadius: 32, backgroundColor: '#10b981', alignItems: 'center', justifyContent: 'center', shadowColor: '#10b981', shadowOpacity: 0.35, shadowRadius: 12, shadowOffset: { width: 0, height: 6 }, elevation: 14 }}
        >
          <Ionicons name="scan" size={26} color="#fff" />
        </Pressable>
      </View>
    );
  }

  function Tabs() {
    return (
      <Tab.Navigator screenOptions={{ headerShown: false, tabBarStyle: { display: 'none' }, tabBarHideOnKeyboard: true }} tabBar={(props) => <FloatingTabBar {...props} />}>
        <Tab.Screen name="Home" component={HomeStack} options={{ lazy: true }} />
        <Tab.Screen name="Profile" component={ProfileStack} options={{ lazy: true }} />
      </Tab.Navigator>
    );
  }

  return (
    <GestureHandlerRootView style={styles.container}>
      {firstOpen ? (
        <Onboarding onDone={async () => { await AsyncStorage.setItem('onboardingDone', '1'); setFirstOpen(false); }} />
      ) : (
        <NavigationContainer theme={isDark ? DarkTheme_ : LightTheme}>
          <Stack.Navigator screenOptions={{ headerShown: false, presentation: 'card' }}>
            <Stack.Screen name="Tabs" component={Tabs} />
            <Stack.Screen name="ScanModal" component={ScanScreen} options={{ presentation: 'modal', animation: 'slide_from_bottom' }} />
          </Stack.Navigator>
        </NavigationContainer>
      )}
      <StatusBar style={isDark ? 'light' : 'dark'} />
    </GestureHandlerRootView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  // styles below moved to screen components
});
