#include "project.h"

Project::Project(std::string id, std::string name, std::string description,
                 double base_time, double reward_btc, int difficulty,
                 std::vector<RoleRequirement> required_roles)
    : id(std::move(id))
    , name(std::move(name))
    , description(std::move(description))
    , base_time(base_time)
    , reward_btc(reward_btc)
    , difficulty(difficulty)
    , required_roles(std::move(required_roles))
{
}

double ActiveProject::calc_completion_time(const Project& proj,
                                            const std::vector<AssignedWorkers>& workers) {
    // Sum minimum required workers
    int min_total = 0;
    for (const auto& req : proj.required_roles) {
        min_total += req.count;
    }

    // Sum actual assigned workers weighted by role efficiency
    double efficiency_bonus = 0.0;
    int assigned_total = 0;
    for (const auto& aw : workers) {
        assigned_total += aw.count;
        // Extra workers beyond minimum contribute efficiency
        double role_eff = worker_role_efficiency(aw.role);
        efficiency_bonus += aw.count * role_eff;
    }

    // Minimum efficiency from required staff
    double min_efficiency = 0.0;
    for (const auto& req : proj.required_roles) {
        min_efficiency += req.count * worker_role_efficiency(req.role);
    }

    // Bonus comes from extra efficiency above minimum
    double extra = efficiency_bonus - min_efficiency;
    if (extra < 0) extra = 0;

    // completion_time = base_time / (1 + extra_efficiency_bonus * 0.1)
    double time = proj.base_time / (1.0 + extra * 0.1);

    // Floor at 10% of base time
    double min_time = proj.base_time * 0.1;
    return time < min_time ? min_time : time;
}

double ActiveProject::remaining(double current_time) const {
    double r = end_time - current_time;
    return r > 0 ? r : 0;
}

double ActiveProject::progress(double current_time) const {
    double duration = end_time - start_time;
    if (duration <= 0) return 1.0;
    double elapsed = current_time - start_time;
    if (elapsed >= duration) return 1.0;
    if (elapsed <= 0) return 0.0;
    return elapsed / duration;
}

// WorkerPool implementation
WorkerPool::WorkerPool() {
    // Start with some basic workers
    available[WorkerRole::ProjectManager] = 1;
    available[WorkerRole::Engineer] = 3;
    available[WorkerRole::Designer] = 0;
    available[WorkerRole::Analyst] = 0;
    available[WorkerRole::Hacker] = 0;

    total[WorkerRole::ProjectManager] = 1;
    total[WorkerRole::Engineer] = 3;
    total[WorkerRole::Designer] = 0;
    total[WorkerRole::Analyst] = 0;
    total[WorkerRole::Hacker] = 0;
}

bool WorkerPool::has_enough(const std::vector<RoleRequirement>& reqs) const {
    for (const auto& req : reqs) {
        if (get_available(req.role) < req.count) return false;
    }
    return true;
}

bool WorkerPool::has_available(const std::vector<AssignedWorkers>& assignment) const {
    for (const auto& aw : assignment) {
        if (get_available(aw.role) < aw.count) return false;
    }
    return true;
}

void WorkerPool::lock_workers(const std::vector<AssignedWorkers>& assignment) {
    for (const auto& aw : assignment) {
        available[aw.role] -= aw.count;
        if (available[aw.role] < 0) available[aw.role] = 0;
    }
}

void WorkerPool::unlock_workers(const std::vector<AssignedWorkers>& assignment) {
    for (const auto& aw : assignment) {
        available[aw.role] += aw.count;
        if (available[aw.role] > total[aw.role]) {
            available[aw.role] = total[aw.role];
        }
    }
}

int WorkerPool::get_available(WorkerRole role) const {
    auto it = available.find(role);
    return it != available.end() ? it->second : 0;
}

int WorkerPool::get_total(WorkerRole role) const {
    auto it = total.find(role);
    return it != total.end() ? it->second : 0;
}

void WorkerPool::add_workers(WorkerRole role, int count) {
    total[role] += count;
    available[role] += count;
}
