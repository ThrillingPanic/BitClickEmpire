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

std::string GameEngine::save() const {
    return state_.serialize();
}

bool GameEngine::load(const std::string& data) {
    bool ok = state_.deserialize(data);
    if (ok) last_update_ = std::chrono::steady_clock::now();
    return ok;
}
