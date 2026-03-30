# BitClick Empire

A clicker/idle game where you tap a Bitcoin to build a software empire — inspired by Adventure Capitalist and Cookie Clicker, but themed around software development and tech startups.

## Game Concept

- **Tap the Bitcoin** to earn BTC (in-game currency)
- **Buy businesses** that passively generate BTC over time
- **Scale your empire** from an auto-clicker all the way to a Tech Megacorp
- **Milestone bonuses** at 25, 50, 100, 200, 300, 400 units multiply income
- **Auto-save** on pause/exit

## Businesses

| Name | Base Cost | Base Income/sec | Description |
|------|-----------|----------------|-------------|
| Auto Clicker | 15 | 0.1 | A simple script that clicks for you |
| Unpaid Intern | 100 | 1.0 | Fresh out of bootcamp |
| Freelance Dev | 1,100 | 8.0 | Works from a coffee shop |
| Garage Startup | 12,000 | 47.0 | Two devs, a whiteboard, and a dream |
| Indie Studio | 130,000 | 260.0 | Small team shipping real products |
| SaaS Platform | 1.4M | 1,400 | Monthly recurring revenue |
| Cloud Infrastructure | 20M | 7,800 | Servers everywhere |
| AI Research Lab | 330M | 44,000 | Teaching machines to code |
| Blockchain Network | 5.1B | 260,000 | Decentralized everything |
| Tech Megacorp | 75B | 1.6M | You are the monopoly now |

## Tech Stack

- **Game logic**: C++17 (NDK)
- **UI**: Android Java + XML layouts
- **Bridge**: JNI (native_bridge.cpp ↔ GameBridge.java)
- **Build**: Gradle + CMake

## Project Structure

```
app/src/main/
├── cpp/
│   ├── CMakeLists.txt       # Native build config
│   ├── game_engine.h/cpp    # Top-level engine (timing, API)
│   ├── game_state.h/cpp     # Game data, businesses, save/load
│   ├── upgrade.h/cpp        # Individual business/upgrade logic
│   └── native_bridge.cpp    # JNI function exports
├── java/com/bitclickempire/game/
│   ├── MainActivity.java    # UI, game loop, RecyclerView
│   └── GameBridge.java      # JNI native method declarations
└── res/
    ├── layout/              # activity_main.xml, item_upgrade.xml
    ├── drawable/            # bitcoin_bg.xml (circle drawable)
    └── values/              # colors, strings, styles (dark theme)
```

## Building

1. Open in Android Studio (Arctic Fox or later)
2. Ensure NDK and CMake are installed via SDK Manager
3. Build & Run on device or emulator (API 24+)

## Next Steps

- [ ] Add prestige/reset mechanic (reset for permanent multipliers)
- [ ] Add achievement system
- [ ] Add offline earnings calculation
- [ ] Add upgrade tiers (speed boosts, profit multipliers per business)
- [ ] Add sound effects and particle animations on click
- [ ] Add manager system (auto-collect from businesses)
- [ ] Add angel investors (prestige currency)
