# 🧊 Penguin Catch – A Real-Time Location-Based Mini Game

**Penguin Catch** is a dynamic and engaging grid-based Android game where you control a penguin dodging snowflakes and collecting coins. The twist? Every game session is recorded with your real-world location and beautifully visualized on a map alongside a score table.

---

## 🎮 Features

- 🐧 Control the penguin with on-screen buttons or tilt controls if enabled
- ❄️ Snowflakes fall from the sky — collision causes you to lose lives
- 💰 Collect coins for bonus points
- 📍 Automatically record game location using GPS (if permission granted)
- 📊 Final screen includes:
  - A table of the 10 most recent games
  - An interactive map with markers for each game session
  - Clickable rows: tap a score to zoom the map to that game's location

---


## 📱 Tech Stack

- Kotlin + Android SDK
- Google Maps SDK
- SharedPreferences (local data persistence)
- Gson (score serialization)
- Material UI components

---

## 🗺️ End Screen Overview
Score Table: Displays time, score, and coordinates

MapView: Shows markers for all recorded games

Interactivity: Clicking a score row animates the map to that game's marker

Navigation: One-tap return to main screen to replay instantly

---


## 🔐 Location Permission

This app requests:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>

```
---

# 🧊 Built with ❤️ by Rotem
A fast-paced, location-enhanced game that blends reflexes with data tracking.

---
