#include "game_engine.h"

GameEngine::GameEngine()
    : last_update_(std::chrono::steady_clock::now())
{
}

double GameEngine::on_click() {
    return state_.click();
}

double GameEngine::update() {
    auto now = std::chrono::steady_clock::now();
    double dt = std::chrono::duration<double>(now - last_update_).count();
    last_update_ = now;

    double before = state_.coins;
    state_.tick(dt);
    return state_.coins - before;
}

bool GameEngine::buy_upgrade(int index) {
    return state_.buy_upgrade(index);
}

double GameEngine::get_coins() const { return state_.coins; }
double GameEngine::get_total_earned() const { return state_.total_coins_earned; }
double GameEngine::get_income_per_second() const { return state_.total_income_per_second(); }
double GameEngine::get_click_power() const { return state_.click_power * state_.click_multiplier; }
int GameEngine::get_upgrade_count() const { return state_.upgrade_count(); }

std::string GameEngine::get_upgrade_name(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.upgrades.size())) return "";
    return state_.upgrades[index].name;
}

std::string GameEngine::get_upgrade_desc(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.upgrades.size())) return "";
    return state_.upgrades[index].description;
}

double GameEngine::get_upgrade_cost(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.upgrades.size())) return 0;
    return state_.upgrades[index].current_cost();
}

double GameEngine::get_upgrade_income(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.upgrades.size())) return 0;
    return state_.upgrades[index].income_per_second();
}

int GameEngine::get_upgrade_owned(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.upgrades.size())) return 0;
    return state_.upgrades[index].owned;
}

// Project system implementations
int GameEngine::get_project_count() const {
    return state_.project_count();
}

std::string GameEngine::get_project_name(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.projects.size())) return "";
    return state_.projects[index].name;
}

std::string GameEngine::get_project_desc(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.projects.size())) return "";
    return state_.projects[index].description;
}

double GameEngine::get_project_base_time(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.projects.size())) return 0;
    return state_.projects[index].base_time;
}

double GameEngine::get_project_reward(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.projects.size())) return 0;
    return state_.projects[index].reward_btc;
}

int GameEngine::get_project_difficulty(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.projects.size())) return 0;
    return state_.projects[index].difficulty;
}

int GameEngine::get_project_required_role_count(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.projects.size())) return 0;
    return static_cast<int>(state_.projects[index].required_roles.size());
}

std::string GameEngine::get_project_required_role_name(int proj_index, int role_index) const {
    if (proj_index < 0 || proj_index >= static_cast<int>(state_.projects.size())) return "";
    const auto& roles = state_.projects[proj_index].required_roles;
    if (role_index < 0 || role_index >= static_cast<int>(roles.size())) return "";
    return worker_role_name(roles[role_index].role);
}

int GameEngine::get_project_required_role_amount(int proj_index, int role_index) const {
    if (proj_index < 0 || proj_index >= static_cast<int>(state_.projects.size())) return 0;
    const auto& roles = state_.projects[proj_index].required_roles;
    if (role_index < 0 || role_index >= static_cast<int>(roles.size())) return 0;
    return roles[role_index].count;
}

// Active projects
int GameEngine::get_active_project_count() const {
    return state_.active_project_count();
}

std::string GameEngine::get_active_project_name(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.active_projects.size())) return "";
    int pi = state_.active_projects[index].project_index;
    if (pi < 0 || pi >= static_cast<int>(state_.projects.size())) return "";
    return state_.projects[pi].name;
}

double GameEngine::get_active_project_progress(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.active_projects.size())) return 0;
    return state_.active_projects[index].progress(state_.game_time);
}

double GameEngine::get_active_project_remaining(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.active_projects.size())) return 0;
    return state_.active_projects[index].remaining(state_.game_time);
}

bool GameEngine::is_active_project_completed(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.active_projects.size())) return false;
    return state_.active_projects[index].completed;
}

bool GameEngine::start_project(int project_index, const std::vector<AssignedWorkers>& assignment) {
    return state_.start_project(project_index, assignment);
}

bool GameEngine::claim_project(int active_index) {
    return state_.claim_project(active_index);
}

// Worker pool
int GameEngine::get_worker_available(int role) const {
    return state_.workers.get_available(static_cast<WorkerRole>(role));
}

int GameEngine::get_worker_total(int role) const {
    return state_.workers.get_total(static_cast<WorkerRole>(role));
}

void GameEngine::hire_worker(int role, double cost) {
    state_.hire_worker(static_cast<WorkerRole>(role), cost);
}

double GameEngine::get_game_time() const {
    return state_.game_time;
}

std::string GameEngine::save() const {
    return state_.serialize();
}

bool GameEngine::load(const std::string& data) {
    bool ok = state_.deserialize(data);
    if (ok) last_update_ = std::chrono::steady_clock::now();
    return ok;
}
