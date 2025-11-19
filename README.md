🌌 Celestia

An Android app built with Kotlin + Jetpack Compose

Celestia is a student-built Android application that brings space weather, Earth-orbit details, and astronomical insights together in one clean dashboard. It provides real-time geomagnetic activity (Kp Index), live ISS tracking, lunar phase data, and upcoming asteroid approaches — all powered by modern Android technologies such as Jetpack Compose, MVVM, Room, Retrofit, Firebase, and Google Maps.

This project was built as part of an ongoing learning journey into Android development, clean architecture, API integration, and UI/UX principles.

✨ Features
🏠 Dashboard

A single screen showing:

Current Kp Index (geomagnetic activity level)

Real-time ISS location (coordinates, altitude, velocity)

Current lunar phase (illumination %, age, moonrise/moonset)

Featured asteroid (next close approach + hazard status)

📈 Kp Index Screen

Hour-by-hour geomagnetic readings

Grouped by day for easier reading

Includes color status levels (e.g., “Active”, “Minor Storm”, etc.)

🛰 ISS Location Screen

Live ISS coordinates, altitude, and speed

Google Maps Compose marker tracking the station’s position

Shows number of astronauts currently onboard

🌙 Lunar Phase Screen

Current phase, illumination, age, moonrise, and moonset

List of upcoming major phases (New Moon → Full Moon)

☄️ Asteroid Tracking Screen

Displays NASA NEO asteroid data

Highlights hazardous asteroids

Next close approach, velocity, miss distance, orbit details

⚙️ Settings Screen

Dark Mode toggle

12h/24h time format

Refresh on launch

Device location permission toggle

Firebase Sign-Out

🛠 Tech Stack
Languages & Frameworks

Kotlin

Jetpack Compose

Coroutines + Flow

Architecture & Data

MVVM + Repository Pattern

Room Database

DataStore Preferences

Networking & APIs

Retrofit (NASA NEO, NOAA Kp Index, ISS API, Lunar API)

Google Maps Compose

Firebase Authentication

Other Tools

Live data formatting utilities (FormatUtils.kt, TimeUtils.kt)

Modular package organization

🏗 Architecture Overview (Beginner-Friendly)

Celestia uses a clean, layered architecture to keep code maintainable:

UI (Screens)  
↓  
ViewModels  
↓  
Repository  
↓  
Room DB + Retrofit APIs


UI (Compose Screens): Displays data

ViewModels: Holds state + business logic

Repositories: Single source of truth

Room: Caches data for offline use

Retrofit: Fetches remote API data

DataStore: Saves user settings

This structure keeps the app organized, testable, and easy to expand.

📁 Project Structure
com.example.celestia/
│
├── data/
│   ├── db/              // Room database + DAOs
│   ├── model/           // Data classes
│   ├── repository/      // Repositories for each feature
│
├── ui/
│   ├── screens/         // Compose screens (Dashboard, ISS, etc.)
│   ├── viewmodel/       // ViewModels
│
└── utils/               // FormatUtils, TimeUtils, helpers


This is a high-level overview — files are grouped by feature and responsibility.

▶️ How to Run the Project
Requirements

Android Studio Ladybug or newer

Android SDK 24+

Google Maps API key

Firebase project for Authentication

Internet connection (APIs require network)

1. Clone the project
   git clone https://github.com/your-username/celestia.git
   cd celestia

2. Open in Android Studio

File → Open

Select the project folder

Let Gradle sync

3. Add API Keys

Depending on your API configuration, you may need:

Service	Key Location
NASA NEO	local.properties or BuildConfig field
IPGeolocation (lunar API)	BuildConfig or secure key file
Google Maps	local.properties → MAPS_API_KEY="your_key"
Firebase	Place google-services.json in /app

Example using local.properties:

NASA_API_KEY=your_key_here
LUNAR_API_KEY=your_key_here
MAPS_API_KEY=your_key_here


And load them using BuildConfig or your preferred approach.

4. Run on device/emulator

Choose Pixel emulator or physical device

Click Run ▶ in Android Studio

🔧 Versioning & Branch Workflow

Celestia uses a simple and clean Git workflow:

main branch – stable, release-ready

development branch – active work

Feature branches optional (ex: kp_refactor, ui-polish)

Releases tagged as:

v1.0.0 (initial release)

v1.0.1 (patches, UI improvements)

This mirrors a real-world development workflow in a portfolio-friendly way.

🌟 Future Improvements (Ideas)

Widget support (Kp Index, Moon Phase)

Alerts for geomagnetic storms

ISS pass predictions for user location

Offline caching improvements

Theming improvements (dynamic colors)

Astronaut profiles from Wikipedia API

Onboarding tutorial screen

📡 Data Sources & Credits

Celestia uses publicly available scientific data from:

NOAA – Kp Index & space weather

NASA NEO API – asteroid close approaches

Open Notify / ISS API – ISS location & crew

IPGeolocation – lunar phase & rise/set times

Google Maps Platform

Big thanks to these organizations for providing free, open data.

📄 License (Optional)

If you want a permissive open-use license:

MIT License
Copyright (c) 2025
Permission is hereby granted, free of charge, to any person obtaining a copy...


Or leave the project unlicensed for now — your choice.

🙌 Final Notes

Celestia is a learning-driven project built to practice real Android development patterns, Jetpack Compose UI, API integration, and clean architecture. The goal is clarity, maintainability, and real-time space data visualized in a simple way.

If you're reviewing this project for a portfolio or academic context, thank you!