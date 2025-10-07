# Waste Wizard

Snap a photo → AI identifies the item → shows how to dispose/recycle it.

## Stack

* Mobile: React Native (Expo, TypeScript)
* Backend: FastAPI + TensorFlow (SSD MobileNet v2 on TACO)
* Data: JSON rules (location-aware)

## Roadmap

* MVP: Capture → Infer → Guidance (done)
* Next: UI polish, Settings (API URL), rules admin and localization
* Future: On‑device model, CI/CD \& deploy (Docker/Cloud Run)

## Current Status

* Forntend -> Basic structure is initalised (waiting for backend).
* Backend -> Not yet initalised.
