#include "boost.h"
#include <sstream>
#include <iomanip>

Boost::Boost(std::string id, std::string name, std::string description,
             BoostType type, int max_level, double base_cost,
             double cost_multiplier, double value_per_level)
    : id(std::move(id))
    , name(std::move(name))
    , description(std::move(description))
    , type(type)
    , level(0)
    , max_level(max_level)
    , base_cost(base_cost)
    , cost_multiplier(cost_multiplier)
    , value_per_level(value_per_level)
{
}

double Boost::current_cost() const {
    return base_cost * std::pow(cost_multiplier, level);
}

double Boost::current_value() const {
    return value_per_level * level;
}

bool Boost::buy(double& coins) {
    if (level >= max_level) return false;
    double cost = current_cost();
    if (coins < cost) return false;
    coins -= cost;
    level++;
    return true;
}

bool Boost::is_maxed() const {
    return level >= max_level;
}

std::string Boost::effect_text() const {
    std::ostringstream oss;
    switch (type) {
        case BoostType::IdleEarnings:
            oss << (int)(current_value() * 100) << "% offline income";
            if (!is_maxed()) oss << " → " << (int)((level + 1) * value_per_level * 100) << "%";
            break;
        case BoostType::IdleDuration: {
            int mins = (int)(current_value() / 60);
            oss << mins << "min max offline";
            if (!is_maxed()) oss << " → " << (int)((level + 1) * value_per_level / 60) << "min";
            break;
        }
        case BoostType::ClickPower:
            oss << "+" << (int)current_value() << " click power";
            if (!is_maxed()) oss << " → +" << (int)((level + 1) * value_per_level);
            break;
        case BoostType::ClickMultiplier:
            oss << std::fixed << std::setprecision(1) << (1.0 + current_value()) << "x clicks";
            if (!is_maxed()) oss << " → " << (1.0 + (level + 1) * value_per_level) << "x";
            break;
        case BoostType::IncomeBoost:
            oss << std::fixed << std::setprecision(2) << (1.0 + current_value()) << "x income";
            if (!is_maxed()) oss << " → " << (1.0 + (level + 1) * value_per_level) << "x";
            break;
        case BoostType::CriticalClick:
            oss << (int)(current_value() * 100) << "% crit chance (10x)";
            if (!is_maxed()) oss << " → " << (int)((level + 1) * value_per_level * 100) << "%";
            break;
        case BoostType::AutoClicker:
            oss << std::fixed << std::setprecision(1) << current_value() << " auto-clicks/sec";
            if (!is_maxed()) oss << " → " << (level + 1) * value_per_level;
            break;
        case BoostType::LuckyBonus:
            oss << "Lv." << level << " lucky bonus every 60s";
            if (!is_maxed()) oss << " → Lv." << (level + 1);
            break;
    }
    return oss.str();
}
