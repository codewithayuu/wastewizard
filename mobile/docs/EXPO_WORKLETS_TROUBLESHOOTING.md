# Expo Go Worklets mismatch troubleshooting log

- When: 2025-10-07T21:21:20+05:30
- Project: `mobile/` (Expo SDK 54)
- Platform: Linux dev host, Expo Go on device

## Environment
- Node: v20.19.5 (nvm)
- npm: 10.8.2
- Expo: `expo@54.0.12`
- React Native: `0.81.4`
- Reanimated: `react-native-reanimated@4.1.2`
- Babel plugin: `'react-native-reanimated/plugin'` present in `mobile/babel.config.js`

## Symptom
- Runtime red screen in Expo Go: WorkletsError
  - "Mismatch between JavaScript part and native part of Worklets (0.6.0 vs 0.5.1)"

## Root cause (summary)
- Expo Go ships native binaries. For Reanimated v4, the JS side depends on `react-native-worklets` (not `react-native-worklets-core`).
- The device’s native Worklets inside Expo Go expects `0.5.x`, but the JS dependency resolved to `0.6.x` (or a wrong package like `react-native-worklets-core`).

## Actions taken

1) Installed toolchain and dependencies
```
# nvm, Node LTS, npm
curl -fsSL https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
. "$HOME/.nvm/nvm.sh" && nvm install --lts && nvm use --lts

# install project deps
(cd mobile && npm ci)
```

2) Initial run to reproduce and validate
```
(cd mobile && npx expo start)
# Observed the mismatch error on device (screenshot provided by user)
```

3) Inspect currently installed packages
```
(cd mobile && npm ls react-native-worklets-core react-native-reanimated)
# Initially: reanimated@4.1.2; worklets-core present when installed, then removed
```

4) Remove incorrect package and ensure it’s not declared
```
(cd mobile && npm remove react-native-worklets-core)
(cd mobile && npm pkg delete dependencies.react-native-worklets-core)
```

5) Use Expo to install compatible packages
```
# Install the correct package required by Reanimated v4
(cd mobile && npx expo install react-native-worklets@0.5.1)
# Install missing peer dependency reported by expo-doctor
(cd mobile && npx expo install expo-font)
# Align reanimated to Expo SDK 54 (redundant check)
(cd mobile && npx expo install react-native-reanimated)
```

6) Verify installed versions
```
(cd mobile && npm ls react-native-worklets react-native-reanimated)
# Result:
# react-native-worklets@0.5.1
# react-native-reanimated@4.1.2
```

7) Clear caches and restart Metro
```
# Stop any running Metro/Expo
pkill -f "node .*expo.*start" || true

# Start clean
(cd mobile && npx expo start --clear)
```

8) Environment health check
```
(cd mobile && npx expo-doctor)
# Before installing, it warned:
#  - Missing peer dependency: expo-font (required by @expo/vector-icons)
#  - Missing peer dependency: react-native-worklets (required by reanimated)
# After installing above, warnings cleared.
```

## Current state
- `mobile/package.json` no longer contains `react-native-worklets-core`.
- `react-native-worklets@0.5.1` is installed and deduped under `react-native-reanimated@4.1.2`.
- Expo dev server is running with a cleared bundler cache.

## How to verify on device
- Fully quit Expo Go on your phone (swipe away) and reopen.
- Ensure the phone and computer are on the same network.
- Scan the QR shown in the terminal where Expo is running.
- If the red screen persists:
  - Update Expo Go from the app store.
  - Run a stricter clean: delete `mobile/node_modules` and `mobile/package-lock.json`, then `npm i`, then `npx expo start -c`.
  - Re-scan the QR.

## If you need Worklets 0.6.0
- Expo Go will not work because its native binary does not include 0.6.x.
- Use a custom dev client instead:
```
(cd mobile && npx expo install expo-dev-client)
(cd mobile && npm i react-native-worklets@0.6.0 --save)
(cd mobile && npx expo run:android) # or: npx expo run:ios (on macOS)
(cd mobile && npx expo start --dev-client)
```

## Appendix: references
- `mobile/app.json` (Expo config)
- `mobile/babel.config.js` includes `'react-native-reanimated/plugin'`
- Entry file `mobile/index.ts` imports `'react-native-gesture-handler'` first
