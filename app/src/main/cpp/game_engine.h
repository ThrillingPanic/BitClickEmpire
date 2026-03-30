#pragma once

#include "game_state.h"
#include "mines_game.h"
#include <chrono>
#include <string>
#include <vector>

class GameEngine {
public:
    GameEngine();

    double on_click();
    double update();
    bool buy_upgrade(int index);

    // Getters for UI
    double get_coins() const;
    double get_total_earned() const;
    double get_income_per_second() const;
    double get_click_power() const;
    int get_upgrade_count() const;

    // Upgrade info
    std::string get_upgrade_name(int index) const;
    std::string get_upgrade_desc(int index) const;
    double get_upgrade_cost(int index) const;
    double get_upgrade_income(int index) const;
    int get_upgrade_owned(int index) const;

    // Project system
    int get_project_count() const;
    std::string get_project_name(int index) const;
    std::string get_project_desc(int index) const;
    double get_project_base_time(int index) const;
    double get_project_reward(int index) const;
    int get_project_difficulty(int index) const;
    int get_project_required_role_count(int index) const;
    std::string get_project_required_role_name(int proj_index, int role_index) const;
    int get_project_required_role_amount(int proj_index, int role_index) const;

    // Active projects
    int get_active_project_count() const;
    std::string get_active_project_name(int index) const;
    double get_active_project_progress(int index) const;
    double get_active_project_remaining(int index) const;
    bool is_active_project_completed(int index) const;
    bool start_project(int project_index, const std::vector<AssignedWorkers>& assignment);
    bool claim_project(int active_index);

    // Worker pool
    int get_worker_available(int role) const;
    int get_worker_total(int role) const;
    void hire_worker(int role, double cost);
    double get_game_time() const;

    // Boost system
    int get_boost_count() const;
    std::string get_boost_name(int index) const;
    std::string get_boost_desc(int index) const;
    double get_boost_cost(int index) const;
    int get_boost_level(int index) const;
    int get_boost_max_level(int index) const;
    std::string get_boost_effect(int index) const;
    bool buy_boost(int index);
    double get_idle_earnings_pct() const;
    double get_idle_duration_max() const;
    double get_last_lucky_bonus() const;

    // Mines gambling
    bool mines_start(int mine_count, double bet);
    int mines_reveal(int index);
    double mines_cash_out();
    int mines_get_state() const;
    int mines_get_mine_count() const;
    double mines_get_bet() const;
    double mines_get_multiplier() const;
    double mines_get_next_multiplier() const;
    double mines_get_potential_win() const;
    int mines_get_tiles_revealed() const;
    int mines_get_tile_state(int index) const;
    double mines_get_max_bet() const;

    // Save/Load
    std::string save() const;
    bool load(const std::string& data);

private:
    GameState state_;
    MinesGame mines_game_;
    std::chrono::steady_clock::time_point last_update_;
};
