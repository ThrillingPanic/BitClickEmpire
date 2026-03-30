#include "upgrade.h"

Upgrade::Upgrade(std::string id, std::string name, std::string description,
                 double base_cost, double cost_multiplier, double base_income)
    : id(std::move(id))
    , name(std::move(name))
    , description(std::move(description))
    , base_cost(base_cost)
    , cost_multiplier(cost_multiplier)
    , base_income(base_income)
    , owned(0)
    , milestone_multiplier(1)
{
}

double Upgrade::current_cost() const {
    return base_cost * std::pow(cost_multiplier, owned);
}

double Upgrade::income_per_second() const {
    if (owned == 0) return 0.0;
    return base_income * owned * milestone_multiplier;
}

bool Upgrade::buy(double& coins) {
    double cost = current_cost();
    if (coins < cost) return false;
    coins -= cost;
    owned++;
    check_milestones();
    return true;
}

void Upgrade::check_milestones() {
    milestone_multiplier = 1;
    if (owned >= 25)  milestone_multiplier *= 2;
    if (owned >= 50)  milestone_multiplier *= 2;
    if (owned >= 100) milestone_multiplier *= 2;
    if (owned >= 200) milestone_multiplier *= 2;
    if (owned >= 300) milestone_multiplier *= 2;
    if (owned >= 400) milestone_multiplier *= 4;
}
