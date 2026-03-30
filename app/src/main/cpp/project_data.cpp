#include "project_data.h"

std::vector<Project> create_default_projects() {
    std::vector<Project> projects;

    // Tier 1: Simple projects (low cost, short time)
    projects.emplace_back(
        "bug_fix", "Bug Fix Sprint",
        "Squash some minor bugs. Easy money.",
        30, 50, 1,
        std::vector<RoleRequirement>{{WorkerRole::Engineer, 1}}
    );

    projects.emplace_back(
        "landing_page", "Landing Page",
        "Build a quick marketing page for a client.",
        60, 150, 1,
        std::vector<RoleRequirement>{
            {WorkerRole::Engineer, 1},
            {WorkerRole::Designer, 1}
        }
    );

    projects.emplace_back(
        "code_review", "Code Audit",
        "Review legacy code. Find the skeletons.",
        45, 100, 1,
        std::vector<RoleRequirement>{
            {WorkerRole::Engineer, 2}
        }
    );

    // Tier 2: Medium projects
    projects.emplace_back(
        "mobile_app", "Mobile App MVP",
        "Ship a minimum viable product. Move fast.",
        180, 800, 2,
        std::vector<RoleRequirement>{
            {WorkerRole::ProjectManager, 1},
            {WorkerRole::Engineer, 2}
        }
    );

    projects.emplace_back(
        "data_pipeline", "Data Pipeline",
        "ETL pipeline for a growing startup.",
        150, 600, 2,
        std::vector<RoleRequirement>{
            {WorkerRole::Engineer, 2},
            {WorkerRole::Analyst, 1}
        }
    );

    projects.emplace_back(
        "website_redesign", "Website Redesign",
        "Modern facelift for an enterprise client.",
        240, 1200, 2,
        std::vector<RoleRequirement>{
            {WorkerRole::ProjectManager, 1},
            {WorkerRole::Engineer, 1},
            {WorkerRole::Designer, 2}
        }
    );

    // Tier 3: Complex projects
    projects.emplace_back(
        "saas_platform", "SaaS Platform",
        "Full-stack SaaS with billing and auth.",
        600, 5000, 3,
        std::vector<RoleRequirement>{
            {WorkerRole::ProjectManager, 1},
            {WorkerRole::Engineer, 3},
            {WorkerRole::Designer, 1}
        }
    );

    projects.emplace_back(
        "security_audit", "Security Audit",
        "Penetration testing for a bank. High stakes.",
        480, 4000, 3,
        std::vector<RoleRequirement>{
            {WorkerRole::Engineer, 2},
            {WorkerRole::Hacker, 2},
            {WorkerRole::Analyst, 1}
        }
    );

    // Tier 4: Major projects
    projects.emplace_back(
        "ai_product", "AI Product Launch",
        "Build and ship an AI-powered product.",
        1200, 15000, 4,
        std::vector<RoleRequirement>{
            {WorkerRole::ProjectManager, 2},
            {WorkerRole::Engineer, 4},
            {WorkerRole::Analyst, 2}
        }
    );

    projects.emplace_back(
        "blockchain_infra", "Blockchain Infrastructure",
        "Decentralized ledger for enterprise. Very web3.",
        1800, 30000, 4,
        std::vector<RoleRequirement>{
            {WorkerRole::ProjectManager, 1},
            {WorkerRole::Engineer, 5},
            {WorkerRole::Hacker, 2},
            {WorkerRole::Analyst, 1}
        }
    );

    // Tier 5: Mega projects
    projects.emplace_back(
        "space_tech", "Space Tech Contract",
        "Build mission-critical software for orbit.",
        3600, 100000, 5,
        std::vector<RoleRequirement>{
            {WorkerRole::ProjectManager, 3},
            {WorkerRole::Engineer, 6},
            {WorkerRole::Designer, 2},
            {WorkerRole::Analyst, 3},
            {WorkerRole::Hacker, 2}
        }
    );

    projects.emplace_back(
        "govt_defense", "Government Defense System",
        "Classified. You'll know if you need to know.",
        7200, 500000, 5,
        std::vector<RoleRequirement>{
            {WorkerRole::ProjectManager, 4},
            {WorkerRole::Engineer, 8},
            {WorkerRole::Hacker, 4},
            {WorkerRole::Analyst, 4}
        }
    );

    return projects;
}
