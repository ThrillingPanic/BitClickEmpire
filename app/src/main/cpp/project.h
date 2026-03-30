#pragma once

#include <string>
#include <vector>
#include <map>
#include <cmath>

// Worker roles available in the game
enum class WorkerRole {
    ProjectManager,
    Engineer,
    Designer,
    Analyst,
    Hacker
};

// String conversion helpers
inline const char* worker_role_name(WorkerRole role) {
    switch (role) {
        case WorkerRole::ProjectManager: return "Project Manager";
        case WorkerRole::Engineer:       return "Engineer";
        case WorkerRole::Designer:       return "Designer";
        case WorkerRole::Analyst:        return "Analyst";
        case WorkerRole::Hacker:         return "Hacker";
    }
    return "Unknown";
}

inline const char* worker_role_id(WorkerRole role) {
    switch (role) {
        case WorkerRole::ProjectManager: return "pm";
        case WorkerRole::Engineer:       return "eng";
        case WorkerRole::Designer:       return "des";
        case WorkerRole::Analyst:        return "ana";
        case WorkerRole::Hacker:         return "hak";
    }
    return "unk";
}

inline WorkerRole worker_role_from_id(const std::string& id) {
    if (id == "pm")  return WorkerRole::ProjectManager;
    if (id == "eng") return WorkerRole::Engineer;
    if (id == "des") return WorkerRole::Designer;
    if (id == "ana") return WorkerRole::Analyst;
    if (id == "hak") return WorkerRole::Hacker;
    return WorkerRole::Engineer;
}

// Efficiency multiplier per role type
inline double worker_role_efficiency(WorkerRole role) {
    switch (role) {
        case WorkerRole::ProjectManager: return 1.5;  // managers boost efficiency
        case WorkerRole::Engineer:       return 1.2;   // engineers boost speed
        case WorkerRole::Designer:       return 1.0;
        case WorkerRole::Analyst:        return 1.1;
        case WorkerRole::Hacker:         return 1.3;
    }
    return 1.0;
}

// A role requirement for a project
struct RoleRequirement {
    WorkerRole role;
    int count;
};

// A project template that can be started
struct Project {
    std::string id;
    std::string name;
    std::string description;
    double base_time;          // seconds to complete with minimum staff
    double reward_btc;
    int difficulty;            // 1-5 stars
    std::vector<RoleRequirement> required_roles;

    Project() = default;
    Project(std::string id, std::string name, std::string description,
            double base_time, double reward_btc, int difficulty,
            std::vector<RoleRequirement> required_roles);
};

// Workers assigned to a specific active project
struct AssignedWorkers {
    WorkerRole role;
    int count;
};

// An active (in-progress or completed) project instance
struct ActiveProject {
    int project_index;                       // index into projects list
    std::vector<AssignedWorkers> assigned;   // workers committed
    double start_time;                       // game-time when started (seconds since epoch)
    double end_time;                         // calculated finish time
    bool completed;                          // timer reached zero
    bool claimed;                            // reward collected

    ActiveProject() : project_index(-1), start_time(0), end_time(0),
                      completed(false), claimed(false) {}

    // Calculate completion time with efficiency bonus from extra staff
    static double calc_completion_time(const Project& proj,
                                        const std::vector<AssignedWorkers>& workers);

    // Get remaining seconds (0 if done)
    double remaining(double current_time) const;

    // Get progress 0.0-1.0
    double progress(double current_time) const;
};

// The player's worker pool
struct WorkerPool {
    std::map<WorkerRole, int> available;  // free workers
    std::map<WorkerRole, int> total;      // total owned

    WorkerPool();

    bool has_enough(const std::vector<RoleRequirement>& reqs) const;
    bool has_available(const std::vector<AssignedWorkers>& assignment) const;
    void lock_workers(const std::vector<AssignedWorkers>& assignment);
    void unlock_workers(const std::vector<AssignedWorkers>& assignment);
    int get_available(WorkerRole role) const;
    int get_total(WorkerRole role) const;
    void add_workers(WorkerRole role, int count);
};
