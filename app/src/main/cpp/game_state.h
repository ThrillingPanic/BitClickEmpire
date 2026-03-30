#pragma once

#include "upgrade.h"
#include <vector>
#include <string>

// Holds the full game state: coins, upgrades, click power, etc.
struct GameState {
    double coins;
    double total_coins_earned;
    double click_power;       // coins per click
    double click_multiplier;  // global click multiplier
    double income_multiplier; // global passive income multiplier
    std::vector<Upgrade> upgrades;

    GameState();

    // Initialize the default set of software empire businesses
    void init_upgrades();

    // Perform a click, returns coins earned
    double click();

    // Advance passive income by dt seconds
    void tick(double dt);

    // Total passive income per second
    double total_income_per_second() const;

    // Buy an upgrade by index, returns success
    bool buy_upgrade(int index);

    // Get the number of upgrades
    int upgrade_count() const;

    // Serialize state to a JSON-like string for saving
    std::string serialize() const;

    // Deserialize state from saved string
    bool deserialize(const std::string& data);
};
