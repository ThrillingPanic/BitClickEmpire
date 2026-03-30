#include "upgrade_data.h"

std::vector<Upgrade> create_default_upgrades() {
    std::vector<Upgrade> upgrades;

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
        1100, 1.15, 10.0
    );
    upgrades.emplace_back(
        "startup", "Garage Startup",
        "Two devs, a whiteboard, and a dream.",
        12000, 1.15, 100.0
    );
    upgrades.emplace_back(
        "studio", "Indie Studio",
        "Small team shipping real products.",
        130000, 1.15, 1000.0
    );
    upgrades.emplace_back(
        "saas", "SaaS Platform",
        "Monthly recurring revenue. The holy grail.",
        1400000, 1.15, 10000.0
    );
    upgrades.emplace_back(
        "cloud", "Cloud Infrastructure",
        "Servers as far as the eye can see.",
        20000000, 1.15, 130000.0
    );
    upgrades.emplace_back(
        "ai_lab", "AI Research Lab",
        "Teaching machines to write code.",
        330000000, 1.15, 2000000.0
    );
    upgrades.emplace_back(
        "blockchain", "Blockchain Network",
        "Decentralized everything. Very web3.",
        5100000000.0, 1.15, 28000000.0
    );
    upgrades.emplace_back(
        "megacorp", "Tech Megacorp",
        "You are the monopoly now.",
        75000000000.0, 1.15, 400000000.0
    );

    return upgrades;
}
