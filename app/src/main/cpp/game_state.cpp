#include "game_state.h"
#include "upgrade_data.h"
#include "project_data.h"
#include <sstream>
#include <cmath>

GameState::GameState()
    : coins(0.0)
    , total_coins_earned(0.0)
    , click_power(1.0)
    , click_multiplier(1.0)
    , income_multiplier(1.0)
    , game_time(0.0)
{
    init_upgrades();
    init_projects();
}

void GameState::init_upgrades() {
    upgrades = create_default_upgrades();
}

void GameState::init_projects() {
    projects = create_default_projects();
}

double GameState::click() {
    double earned = click_power * click_multiplier;
    coins += earned;
    total_coins_earned += earned;
    return earned;
}

void GameState::tick(double dt) {
    game_time += dt;
    double income = total_income_per_second() * dt;
    coins += income;
    total_coins_earned += income;
    update_projects();
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

int GameState::project_count() const {
    return static_cast<int>(projects.size());
}

int GameState::active_project_count() const {
    return static_cast<int>(active_projects.size());
}

bool GameState::start_project(int project_index, const std::vector<AssignedWorkers>& assignment) {
    if (project_index < 0 || project_index >= static_cast<int>(projects.size())) return false;
    const auto& proj = projects[project_index];

    // Verify minimum requirements met
    for (const auto& req : proj.required_roles) {
        int assigned = 0;
        for (const auto& aw : assignment) {
            if (aw.role == req.role) assigned += aw.count;
        }
        if (assigned < req.count) return false;
    }

    // Verify workers available
    if (!workers.has_available(assignment)) return false;

    // Lock workers and create active project
    workers.lock_workers(assignment);

    ActiveProject ap;
    ap.project_index = project_index;
    ap.assigned = assignment;
    ap.start_time = game_time;
    double duration = ActiveProject::calc_completion_time(proj, assignment);
    ap.end_time = game_time + duration;
    ap.completed = false;
    ap.claimed = false;

    active_projects.push_back(ap);
    return true;
}

bool GameState::claim_project(int active_index) {
    if (active_index < 0 || active_index >= static_cast<int>(active_projects.size())) return false;
    auto& ap = active_projects[active_index];
    if (!ap.completed || ap.claimed) return false;

    const auto& proj = projects[ap.project_index];
    coins += proj.reward_btc;
    total_coins_earned += proj.reward_btc;
    ap.claimed = true;

    // Unlock workers
    workers.unlock_workers(ap.assigned);

    // Remove claimed project
    active_projects.erase(active_projects.begin() + active_index);
    return true;
}

void GameState::update_projects() {
    for (auto& ap : active_projects) {
        if (!ap.completed && game_time >= ap.end_time) {
            ap.completed = true;
        }
    }
}

void GameState::hire_worker(WorkerRole role, double cost) {
    if (coins < cost) return;
    coins -= cost;
    workers.add_workers(role, 1);
}

std::string GameState::serialize() const {
    std::ostringstream oss;
    oss.precision(15);
    oss << coins << "\n"
        << total_coins_earned << "\n"
        << click_power << "\n"
        << click_multiplier << "\n"
        << income_multiplier << "\n"
        << game_time << "\n"
        << upgrades.size() << "\n";
    for (const auto& u : upgrades) {
        oss << u.id << " " << u.owned << "\n";
    }
    // Serialize worker pool
    oss << "WORKERS\n";
    oss << workers.total.size() << "\n";
    for (const auto& kv : workers.total) {
        oss << worker_role_id(kv.first) << " " << kv.second << " "
            << workers.get_available(kv.first) << "\n";
    }
    // Serialize active projects
    oss << "PROJECTS\n";
    oss << active_projects.size() << "\n";
    for (const auto& ap : active_projects) {
        oss << ap.project_index << " " << ap.start_time << " " << ap.end_time
            << " " << (ap.completed ? 1 : 0) << " " << (ap.claimed ? 1 : 0)
            << " " << ap.assigned.size() << "\n";
        for (const auto& aw : ap.assigned) {
            oss << worker_role_id(aw.role) << " " << aw.count << "\n";
        }
    }
    return oss.str();
}

bool GameState::deserialize(const std::string& data) {
    std::istringstream iss(data);
    size_t count = 0;
    if (!(iss >> coins >> total_coins_earned >> click_power
              >> click_multiplier >> income_multiplier >> game_time >> count)) {
        // Try legacy format without game_time
        std::istringstream iss2(data);
        game_time = 0;
        if (!(iss2 >> coins >> total_coins_earned >> click_power
                  >> click_multiplier >> income_multiplier >> count)) {
            return false;
        }
        // Parse upgrades from legacy stream
        for (size_t i = 0; i < count; i++) {
            std::string id;
            int owned = 0;
            if (!(iss2 >> id >> owned)) return false;
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
    // Try to read workers section
    std::string marker;
    if (iss >> marker && marker == "WORKERS") {
        size_t worker_count = 0;
        if (iss >> worker_count) {
            for (size_t i = 0; i < worker_count; i++) {
                std::string role_id;
                int tot = 0, avail = 0;
                if (!(iss >> role_id >> tot >> avail)) break;
                WorkerRole role = worker_role_from_id(role_id);
                workers.total[role] = tot;
                workers.available[role] = avail;
            }
        }
    }
    // Try to read active projects section
    if (iss >> marker && marker == "PROJECTS") {
        size_t proj_count = 0;
        if (iss >> proj_count) {
            active_projects.clear();
            for (size_t i = 0; i < proj_count; i++) {
                ActiveProject ap;
                int comp = 0, clm = 0;
                size_t aw_count = 0;
                if (!(iss >> ap.project_index >> ap.start_time >> ap.end_time
                          >> comp >> clm >> aw_count)) break;
                ap.completed = comp != 0;
                ap.claimed = clm != 0;
                for (size_t j = 0; j < aw_count; j++) {
                    std::string role_id;
                    int cnt = 0;
                    if (!(iss >> role_id >> cnt)) break;
                    ap.assigned.push_back({worker_role_from_id(role_id), cnt});
                }
                active_projects.push_back(ap);
            }
        }
    }
    return true;
}
