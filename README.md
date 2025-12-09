# 🌌 Celestia
#### An Android app built with Kotlin + Jetpack Compose

**Celestia is an Android application that brings space weather, orbital data, and astronomical insights together in one elegant dashboard.**

**It provides real-time geomagnetic activity (Kp Index), ISS tracking, lunar phase data, and asteroid information, powered by modern Android technologies such as Jetpack Compose, MVVM, Room, Retrofit, Firebase Authentication, and Google Maps.**

**This project was created as part of an ongoing learning journey into Android development, clean architecture, API integration, and UI/UX design.**

## ✨ Features
#### 🏠 Dashboard
##### A single screen showing:
1. Current Kp Index (geomagnetic activity level)
2. Real-time ISS location (coordinates, altitude, velocity)
3. Current lunar phase (illumination %, age, moonrise/moonset)
4. Featured asteroid (next close approach + hazard status)

#### 📈 Kp Index Screen
1. Hour-by-hour geomagnetic readings
2. Grouped by day for readability
3. Color-coded status labels (e.g., Active, Minor Storm, etc.)

#### 🛰️ ISS Location Screen
1. Live ISS coordinates, altitude, and speed
2. Google Maps Compose marker that updates in real-time
3. Number of astronauts currently onboard

#### 🌙 Lunar Phase Screen
1. Current phase, illumination %, age
2. Moonrise & moonset
3. Upcoming major phases (New Moon → Full Moon)

#### ☄️ Asteroid Tracking Screen
1. NASA NEO asteroid data
2. Hazard classification
3. Miss distance, relative speed, orbit details
4. Next close approach

#### ⚙️ Settings Screen
1. Dark Mode toggle
2. 12h / 24h time format
3. Refresh on launch
4. Device location toggle
5. Firebase Sign-Out

## 🛠️ Tech Stack
#### Languages & UI
- Kotlin
- Jetpack Compose
- Coroutines + Flow

#### Architecture
- MVVM
- Repository Pattern
- Clean modular structure
- Local & Remote Data
- Room Database
- DataStore Preferences
- Retrofit Networking
- Firebase Authentication
- Google Maps Compose

#### Utilities
- FormatUtils.kt (number/time/coordinate formatting)
- TimeUtils.kt (cross-API-level time parsing & formatting)

## 🏗 Architecture Overview

**Celestia uses a clean, layered architecture to keep the codebase maintainable:**

```
UI (Compose Screens)
         ↓
     ViewModels
         ↓
     Repository
         ↓
Room DB + Retrofit APIs
```

#### Roles:

**UI:** Displays data released by ViewModels
**ViewModels:** Holds UI state + business logic
**Repositories:** Single source of truth
**Room:** Caches data for offline use
**Retrofit:** Fetches data from remote APIs
**DataStore:** Persists user preferences

### 📁 Project Structure
```
com.example.celestia/
│
├── data/
│   ├── db/              # Room database + DAOs
│   ├── model/           # Data classes
│   ├── repository/      # Repository layer
│
├── ui/
│   ├── screens/         # Compose screens for each feature
│   ├── viewmodel/       # ViewModels
│
└── utils/               # FormatUtils, TimeUtils, helpers
```

**This is a high-level overview — files are grouped by feature and responsibility.**

### ▶️ How to Run the Project
**Requirements**

- Android Studio Ladybug or newer
- Android SDK 24+
- Google Maps API Key
- Firebase project
- Internet connection

**1. Clone the Project**
git clone https://github.com/tbuhay/celestia.git

```
cd celestia
```

**2. Open in Android Studio**
- File → Open → Select the project folder

- Allow Gradle to sync.

**3. Add API Keys**

Depending on your setup, you may need the following:

- Service	Key Location
- NASA NEO	local.properties or BuildConfig
- Lunar API (IPGeolocation)	BuildConfig or secure key file
- Google Maps	local.properties → MAPS_API_KEY="your_key"
- Firebase	app/google-services.json

**Example (local.properties):**

```
NASA_API_KEY=your_key_here
LUNAR_API_KEY=your_key_here
MAPS_API_KEY=your_key_here
```

### 4. Run on Emulator or Device
- Select a Pixel emulator or plug in a physical device
- Press Run ▶️

### 🔧 Versioning & Branch Workflow

Celestia uses a simple Git workflow:

1. main — stable, release-ready

2. development — active updates

3. Feature branches as needed (kp_refactor, ui-polish, etc.)

**Releases tagged:**

| Version | Focus Status |
|:-------|:----------------------:|
| v1.0.0 | Initial release|
| v1.0.1 | Patches/UI improvements|
| v1.0.2 | Accessibility improvments |

This mirrors real-world portfolio-ready structure.

### 🌟 Future Improvements (Ideas)

| Concept | Priority |
|:--------|:---------|
| Widget support (Kp Index, Moon Phase) | Low |
| Geomagnetic storm alerts | High
| ISS pass predictions | Low |
| Improved offline caching | Medium |
| Dynamic Material You theming | Low |
| Astronaut profiles (Wikipedia API) | Medium |
| Onboarding tutorial | High |

### 📡 Data Sources & Credits

Celestia uses publicly available scientific data:

1. NOAA — Kp Index & space weather
2. NASA NEO API — asteroid approaches
3. Open Notify — ISS location & crew
4. IPGeolocation — lunar phase & rise/set
5. Google Maps Platform

Huge thanks to these organizations for providing open-access data!

### 🙌 Final Notes
**Celestia is a learning-driven project built to explore:**
- Real Android development patterns
- Jetpack Compose UI
- API integration
- Clean architecture
- Offline caching strategies
- Firebase Auth
- Google Maps integration

The goal is clarity, maintainability, and real-time space data visualized in a clean and simple way.

If you’re reviewing this project for a portfolio or academic evaluation — thank you!

### 📦 Download APK
➡️ **You can download the latest Celestia build here:**
https://drive.google.com/file/d/1xMT3SFIMMMmiuCKojGqnbNyKJc5_XvZ1/view?usp=sharing

*(You may need to enable "Install unknown apps" on your device.)*