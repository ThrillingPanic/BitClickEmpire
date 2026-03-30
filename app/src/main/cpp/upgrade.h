#pragma once

#include <string>
#include <cmath>

// Represents a purchasable business/upgrade in the software empire
struct Upgrade {
    std::string id;
    std::string name;
    std::string description;
    double base_cost;
    double cost_multiplier;   // cost scales: base_cost * cost_multiplier^owned
    double base_income;       // coins per second per unit owned
    int owned;
    int milestone_multiplier; // bonus multiplier from milestones

    Upgrade() = default;
    Upgrade(std::string id, std::string name, std::string description,
            double base_cost, double cost_multiplier, double base_income);

    // Current cost to buy one more
    double current_cost() const;

    // Income per second from all owned units (before global multipliers)
    double income_per_second() const;

    // Buy one unit; returns false if not enough coins
    bool buy(double& coins);

    // Check and apply milestone bonuses (at 25, 50, 100, 200, 300, 400 owned)
    void check_milestones();
};
