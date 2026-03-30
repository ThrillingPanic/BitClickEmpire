#pragma once

#include "game_state.h"
#include <chrono>
#include <string>

// Top-level game engine: owns the state, runs the game loop timing
class GameEngine {
public:
    GameEngine();

    // Called when the player taps the bitcoin
    double on_click();

    // Called every frame with real elapsed time; returns coins earned this tick
    double update();

    // Buy upgrade by index
    bool buy_upgrade(int index);

    // Getters for UI
    double get_coins() const;
    double get_total_earned() const;
    double get_income_per_second() const;
    double get_click_power() const;
    int get_upgrade_count() const;

    // Upgrade info for UI
    std::string get_upgrade_name(int index) const;
    std::string get_upgrade_desc(int index) const;
    double get_upgrade_cost(int index) const;
    double get_upgrade_income(int index) const;
    int get_upgrade_owned(int index) const;

    // Save/Load
    std::string save() const;
    bool load(const std::string& data);

private:
    GameState state_;
    std::chrono::steady_clock::time_point last_update_;
};
