#include "boost_data.h"

std::vector<Boost> create_default_boosts() {
    std::vector<Boost> boosts;

    boosts.emplace_back(
        "idle_earnings", "Offline Earnings",
        "Earn a percentage of your income while away from the app.",
        BoostType::IdleEarnings, 10, 500, 2.5, 0.10
        // 10 levels, starts 500 BTC, each level adds 10% (max 100%)
    );

    boosts.emplace_back(
        "idle_duration", "Idle Duration",
        "Increase the maximum time you can earn BTC while offline.",
        BoostType::IdleDuration, 10, 300, 2.0, 1800
        // 10 levels, each level adds 30 minutes (1800s), max 5 hours
    );

    boosts.emplace_back(
        "click_power", "Enhanced Clicks",
        "Each click earns more BTC. Stack it up!",
        BoostType::ClickPower, 20, 100, 1.8, 1.0
        // 20 levels, each level adds +1 click power
    );

    boosts.emplace_back(
        "click_multi", "Click Multiplier",
        "Multiply ALL click earnings. Compound your taps.",
        BoostType::ClickMultiplier, 10, 2000, 3.0, 0.5
        // 10 levels, each level adds x0.5 to click multiplier (max 6x)
    );

    boosts.emplace_back(
        "income_boost", "Income Multiplier",
        "Boost all passive income from your businesses.",
        BoostType::IncomeBoost, 10, 5000, 3.5, 0.25
        // 10 levels, each level adds x0.25 to income multiplier (max 3.5x)
    );

    boosts.emplace_back(
        "crit_click", "Critical Click",
        "Chance for a click to deal 10x damage. Feel the rush.",
        BoostType::CriticalClick, 10, 1500, 2.5, 0.05
        // 10 levels, each level adds 5% crit chance (max 50%)
    );

    boosts.emplace_back(
        "auto_click", "Auto Clicker Pro",
        "Automatically clicks for you every second. Passive tapping.",
        BoostType::AutoClicker, 5, 10000, 4.0, 1.0
        // 5 levels, each level adds 1 auto-click per second
    );

    boosts.emplace_back(
        "lucky_bonus", "Lucky Bonus",
        "Random BTC bonus drops every 60 seconds. Higher level = bigger drops.",
        BoostType::LuckyBonus, 8, 3000, 3.0, 1.0
        // 8 levels, bonus amount scales with income and level
    );

    return boosts;
}
