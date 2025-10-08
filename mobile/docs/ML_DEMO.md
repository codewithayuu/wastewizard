# Optional TensorFlow demo (TFJS + MobileNet)

This doc shows how to add a small TFJS demo to classify an image and wire it into the Scan flow later. The app already ships a stubbed analyzer in `src/screens/ScanScreen.tsx`.

## Install

Run from `mobile/`:

```bash
# Core
npm i @tensorflow/tfjs @tensorflow/tfjs-react-native @tensorflow-models/mobilenet
```

Expo will auto-link `tfjs-react-native`. If you run into native module issues, clear caches and restart Expo with `npx expo start -c`.

## Hook: useMobilenet

Create a hook (choose any allowed path in your repo, e.g. `src/utils/useMobilenet.ts`).

```ts
// src/utils/useMobilenet.ts
import { useEffect, useRef, useState } from 'react';
import * as tf from '@tensorflow/tfjs';
import '@tensorflow/tfjs-react-native';
import * as mobilenet from '@tensorflow-models/mobilenet';

export function useMobilenet() {
  const modelRef = useRef<mobilenet.MobileNet | null>(null);
  const [ready, setReady] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        await tf.ready();
        modelRef.current = await mobilenet.load();
        setReady(true);
      } catch (e: any) {
        setError(e?.message || 'Failed to initialize TF model');
      }
    })();
  }, []);

  const classifyUri = async (uri: string) => {
    if (!modelRef.current) throw new Error('Model not loaded');
    const res = await fetch(uri);
    const buf = await res.arrayBuffer();
    // @ts-ignore decodeImage is provided by tfjs-react-native
    const imageTensor = tf.decodeImage(new Uint8Array(buf), 3);
    const preds = await modelRef.current.classify(imageTensor as any);
    imageTensor.dispose?.();
    return preds; // [{className, probability}]
  };

  return { ready, error, classifyUri };
}
```

## Wire into Scan

Replace the stub call in `src/screens/ScanScreen.tsx` with the TF path:

```ts
// top of ScanScreen.tsx
import { useMobilenet } from '../utils/useMobilenet';

// inside component
const { ready, classifyUri } = useMobilenet();

// in onCapture/onGalleryImport after you have the `uri`
if (ready) {
  const p = await classifyUri(uri);
  const top = p?.[0];
  if (top) {
    setResult({
      item: top.className,
      category: 'Recyclable',
      confidence: top.probability,
      co2eKg: 0.12,
      instructions: 'Generic guidance based on category.',
      prep: ['Rinse if needed', 'Remove labels if required'],
      material: 'plastic', // map as needed
    });
  }
} else {
  // fall back to the existing stub
}
```

## Notes
- This is optional. Keep the current stub to avoid bundling TF during early development.
- TF models increase bundle size and startup time; prefer lazy init and a toggle flag in settings.
- If you enable TF, run with a device or performant emulator. Web support requires a different backend.
