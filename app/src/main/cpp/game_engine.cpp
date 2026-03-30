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

void GameEngine::set_byte_bonus(double multiplier) { state_.set_byte_bonus(multiplier); }
void GameEngine::clear_byte_bonus() { state_.clear_byte_bonus(); }
void GameEngine::add_coins(double amount) { state_.coins += amount; state_.total_coins_earned += amount; }

double GameEngine::get_coins() const { return state_.coins; }
double GameEngine::get_total_earned() const { return state_.total_coins_earned; }
double GameEngine::get_income_per_second() const { return state_.total_income_per_second(); }
double GameEngine::get_click_power() const { return state_.effective_click_power() * state_.effective_click_multiplier(); }
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

bool GameEngine::is_upgrade_locked(int index) const {
    if (index <= 0) return false;  // First upgrade is always unlocked
    if (index >= static_cast<int>(state_.upgrades.size())) return false;
    // An upgrade is locked if the previous one hasn't been owned yet
    return state_.upgrades[index - 1].owned == 0;
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

bool GameEngine::is_project_locked(int index) const {
    if (index <= 0) return false;  // First project is always unlocked
    if (index >= static_cast<int>(state_.projects.size())) return false;
    
    // A project is locked if the previous one hasn't been completed and claimed
    int prev_index = index - 1;
    for (const auto& active : state_.active_projects) {
        if (active.project_index == prev_index && active.claimed) {
            return false;  // Previous project completed, so this one is unlocked
        }
    }
    return true;  // Previous project not completed, so this one is locked
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

// Boost system
int GameEngine::get_boost_count() const {
    return state_.boost_count();
}

std::string GameEngine::get_boost_name(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.boosts.size())) return "";
    return state_.boosts[index].name;
}

std::string GameEngine::get_boost_desc(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.boosts.size())) return "";
    return state_.boosts[index].description;
}

double GameEngine::get_boost_cost(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.boosts.size())) return 0;
    return state_.boosts[index].current_cost();
}

int GameEngine::get_boost_level(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.boosts.size())) return 0;
    return state_.boosts[index].level;
}

int GameEngine::get_boost_max_level(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.boosts.size())) return 0;
    return state_.boosts[index].max_level;
}

std::string GameEngine::get_boost_effect(int index) const {
    if (index < 0 || index >= static_cast<int>(state_.boosts.size())) return "";
    return state_.boosts[index].effect_text();
}

bool GameEngine::buy_boost(int index) {
    return state_.buy_boost(index);
}

double GameEngine::get_idle_earnings_pct() const {
    return state_.get_idle_earnings_pct();
}

double GameEngine::get_idle_duration_max() const {
    return state_.get_idle_duration_max();
}

double GameEngine::get_last_lucky_bonus() const {
    return state_.last_lucky_bonus;
}

// Mines gambling
bool GameEngine::mines_start(int mine_count, double bet) {
    return mines_game_.start(mine_count, bet, state_.coins);
}

int GameEngine::mines_reveal(int index) {
    return mines_game_.reveal(index);
}

double GameEngine::mines_cash_out() {
    double winnings = mines_game_.cash_out(state_.coins);
    if (winnings > 0) {
        state_.total_coins_earned += winnings;
    }
    return winnings;
}

int GameEngine::mines_get_state() const { return static_cast<int>(mines_game_.get_state()); }
int GameEngine::mines_get_mine_count() const { return mines_game_.get_mine_count(); }
double GameEngine::mines_get_bet() const { return mines_game_.get_bet(); }
double GameEngine::mines_get_multiplier() const { return mines_game_.get_multiplier(); }
double GameEngine::mines_get_next_multiplier() const { return mines_game_.get_next_multiplier(); }
double GameEngine::mines_get_potential_win() const { return mines_game_.get_potential_win(); }
int GameEngine::mines_get_tiles_revealed() const { return mines_game_.get_tiles_revealed(); }
int GameEngine::mines_get_tile_state(int index) const { return mines_game_.get_tile_state(index); }
double GameEngine::mines_get_max_bet() const { return state_.coins * 0.10; }

std::string GameEngine::save() const {
    return state_.serialize();
}

bool GameEngine::load(const std::string& data) {
    bool ok = state_.deserialize(data);
    if (ok) last_update_ = std::chrono::steady_clock::now();
    return ok;
}
