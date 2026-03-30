#pragma once

#include <string>
#include <cmath>

enum class BoostType {
    IdleEarnings,   // % of income earned while offline
    IdleDuration,   // max offline earning time (seconds)
    ClickPower,     // flat click power increase
    ClickMultiplier,// multiplier on all clicks
    IncomeBoost,    // multiplier on passive income
    CriticalClick,  // % chance of 10x click
    AutoClicker,    // automatic clicks per second
    LuckyBonus      // random BTC bonus every 60s
};

struct Boost {
    std::string id;
    std::string name;
    std::string description;
    BoostType type;
    int level;
    int max_level;
    double base_cost;
    double cost_multiplier;
    double value_per_level;

    Boost() = default;
    Boost(std::string id, std::string name, std::string description,
          BoostType type, int max_level, double base_cost,
          double cost_multiplier, double value_per_level);

    double current_cost() const;
    double current_value() const;
    bool buy(double& coins);
    bool is_maxed() const;
    std::string effect_text() const;
};
