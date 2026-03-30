#include "game_state.h"
#include <sstream>
#include <cmath>

GameState::GameState()
    : coins(0.0)
    , total_coins_earned(0.0)
    , click_power(1.0)
    , click_multiplier(1.0)
    , income_multiplier(1.0)
{
    init_upgrades();
}

void GameState::init_upgrades() {
    upgrades.clear();
    upgrades.emplace_back(
        "cursor", "Auto Clicker",
        "A simple script that clicks for you.",
        15, 1.15, 0.1
    );
    upgrades.emplace_back(
        "intern", "Unpaid Intern",
        "Fresh out of bootcamp. Writes code... sometimes.",
        100, 1.15, 1.0
    );
    upgrades.emplace_back(
        "freelancer", "Freelance Dev",
        "Works from a coffee shop. Surprisingly productive.",
        1100, 1.15, 8.0
    );
    upgrades.emplace_back(
        "startup", "Garage Startup",
        "Two devs, a whiteboard, and a dream.",
        12000, 1.15, 47.0
    );
    upgrades.emplace_back(
        "studio", "Indie Studio",
        "Small team shipping real products.",
        130000, 1.15, 260.0
    );
    upgrades.emplace_back(
        "saas", "SaaS Platform",
        "Monthly recurring revenue. The holy grail.",
        1400000, 1.15, 1400.0
    );
    upgrades.emplace_back(
        "cloud", "Cloud Infrastructure",
        "Servers as far as the eye can see.",
        20000000, 1.15, 7800.0
    );
    upgrades.emplace_back(
        "ai_lab", "AI Research Lab",
        "Teaching machines to write code.",
        330000000, 1.15, 44000.0
    );
    upgrades.emplace_back(
        "blockchain", "Blockchain Network",
        "Decentralized everything. Very web3.",
        5100000000.0, 1.15, 260000.0
    );
    upgrades.emplace_back(
        "megacorp", "Tech Megacorp",
        "You are the monopoly now.",
        75000000000.0, 1.15, 1600000.0
    );
}

double GameState::click() {
    double earned = click_power * click_multiplier;
    coins += earned;
    total_coins_earned += earned;
    return earned;
}

void GameState::tick(double dt) {
    double income = total_income_per_second() * dt;
    coins += income;
    total_coins_earned += income;
}

double GameState::total_income_per_second() const {
    double total = 0.0;
    for (const auto& u : upgrades) {
        total += u.income_per_second();
    }
    return total * income_multiplier;
}

bool GameState::buy_upgrade(int index) {
    if (index < 0 || index >= static_cast<int>(upgrades.size())) return false;
    return upgrades[index].buy(coins);
}

int GameState::upgrade_count() const {
    return static_cast<int>(upgrades.size());
}

std::string GameState::serialize() const {
    std::ostringstream oss;
    oss.precision(15);
    oss << coins << "\n"
        << total_coins_earned << "\n"
        << click_power << "\n"
        << click_multiplier << "\n"
        << income_multiplier << "\n"
        << upgrades.size() << "\n";
    for (const auto& u : upgrades) {
        oss << u.id << " " << u.owned << "\n";
    }
    return oss.str();
}

bool GameState::deserialize(const std::string& data) {
    std::istringstream iss(data);
    size_t count = 0;
    if (!(iss >> coins >> total_coins_earned >> click_power
              >> click_multiplier >> income_multiplier >> count)) {
        return false;
    }
    for (size_t i = 0; i < count; i++) {
        std::string id;
        int owned = 0;
        if (!(iss >> id >> owned)) return false;
        for (auto& u : upgrades) {
            if (u.id == id) {
                u.owned = owned;
                u.check_milestones();
                break;
            }
        }
    }
    return true;
}
