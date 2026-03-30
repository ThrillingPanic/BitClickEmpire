#pragma once

#include "upgrade.h"
#include "project.h"
#include <vector>
#include <string>

// Holds the full game state: coins, upgrades, click power, etc.
struct GameState {
    double coins;
    double total_coins_earned;
    double click_power;       // coins per click
    double click_multiplier;  // global click multiplier
    double income_multiplier; // global passive income multiplier
    double game_time;         // elapsed game time in seconds
    std::vector<Upgrade> upgrades;
    std::vector<Project> projects;
    std::vector<ActiveProject> active_projects;
    WorkerPool workers;

    GameState();

    void init_upgrades();
    void init_projects();

    double click();
    void tick(double dt);
    double total_income_per_second() const;
    bool buy_upgrade(int index);
    int upgrade_count() const;

    // Project system
    int project_count() const;
    int active_project_count() const;
    bool start_project(int project_index, const std::vector<AssignedWorkers>& assignment);
    bool claim_project(int active_index);
    void update_projects();  // check for completions
    void hire_worker(WorkerRole role, double cost);

    std::string serialize() const;
    bool deserialize(const std::string& data);
};
