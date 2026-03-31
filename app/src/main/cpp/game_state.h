#pragma once

#include "upgrade.h"
#include "project.h"
#include "boost.h"
#include "blackjack_game.h"
#include <vector>
#include <string>

// Holds the full game state: coins, upgrades, click power, etc.
struct GameState {
    double coins;
    double total_coins_earned;
    int64_t total_clicks_made;   // total clicks for unlock conditions
    double click_power;       // coins per click
    double click_multiplier;  // global click multiplier
    double income_multiplier; // global passive income multiplier
    double game_time;         // elapsed game time in seconds
    std::vector<Upgrade> upgrades;
    std::vector<Project> projects;
    std::vector<ActiveProject> active_projects;
    WorkerPool workers;
    std::vector<Boost> boosts;
    BlackjackGame blackjack_game;
    double auto_click_accum;  // accumulator for auto-click timing
    double lucky_timer;       // countdown for next lucky bonus
    double last_lucky_bonus;  // amount of last lucky bonus (for UI)
    double byte_bonus_multiplier; // temporary multiplier from byte collection (1.0 = no bonus)

    GameState();

    void init_upgrades();
    void init_projects();
    void init_boosts();

    double click();
    void tick(double dt);
    double total_income_per_second() const;
    bool buy_upgrade(int index);
    int upgrade_count() const;

    // Boost system
    int boost_count() const;
    bool buy_boost(int index);
    double get_boost_value(BoostType type) const;
    double effective_click_power() const;
    double effective_click_multiplier() const;
    double effective_income_multiplier() const;
    double get_idle_earnings_pct() const;
    double get_idle_duration_max() const;
    double get_crit_chance() const;
    double get_auto_clicks_per_sec() const;
    void set_byte_bonus(double multiplier);
    void clear_byte_bonus();

    // Project system
    int project_count() const;
    int active_project_count() const;
    bool start_project(int project_index, const std::vector<AssignedWorkers>& assignment);
    bool claim_project(int active_index);
    void update_projects();  // check for completions
    void hire_worker(WorkerRole role, double cost);

    // Casino games
    bool is_blackjack_unlocked() const;  // Unlocked after 10,000 clicks

    std::string serialize() const;
    bool deserialize(const std::string& data);
};
