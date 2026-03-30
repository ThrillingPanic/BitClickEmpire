#include "mines_game.h"
#include <chrono>

MinesGame::MinesGame()
    : state_(MINES_INACTIVE)
    , mine_count_(0)
    , safe_revealed_(0)
    , bet_(0)
    , fair_multiplier_(1.0)
    , rng_state_(1)
{
    for (int i = 0; i < GRID_SIZE; i++) {
        mines_[i] = false;
        tile_state_[i] = 0;
    }
}

// xorshift64 PRNG
uint64_t MinesGame::next_rng() {
    rng_state_ ^= rng_state_ << 13;
    rng_state_ ^= rng_state_ >> 7;
    rng_state_ ^= rng_state_ << 17;
    return rng_state_;
}

void MinesGame::shuffle_mines() {
    for (int i = 0; i < GRID_SIZE; i++) {
        mines_[i] = (i < mine_count_);
    }
    // Fisher-Yates shuffle
    for (int i = GRID_SIZE - 1; i > 0; i--) {
        int j = static_cast<int>(next_rng() % static_cast<uint64_t>(i + 1));
        bool tmp = mines_[i];
        mines_[i] = mines_[j];
        mines_[j] = tmp;
    }
}

double MinesGame::fair_step(int step) const {
    int total_hidden = GRID_SIZE - step;
    int safe_hidden = (GRID_SIZE - mine_count_) - step;
    if (safe_hidden <= 0 || total_hidden <= 0) return 0.0;
    return static_cast<double>(total_hidden) / static_cast<double>(safe_hidden);
}

bool MinesGame::start(int mine_count, double bet_amount, double& coins) {
    if (state_ == MINES_ACTIVE) return false;
    if (mine_count < 1 || mine_count > GRID_SIZE - 1) return false;
    if (bet_amount <= 0.0) return false;
    double max_bet = coins * 0.10;
    if (bet_amount > max_bet + 0.01) return false; // small epsilon for fp
    if (bet_amount > coins) return false;

    mine_count_ = mine_count;
    bet_ = bet_amount;
    safe_revealed_ = 0;
    fair_multiplier_ = 1.0;
    state_ = MINES_ACTIVE;

    coins -= bet_amount;

    // Seed RNG from high-resolution clock + game parameters
    auto now = std::chrono::steady_clock::now();
    rng_state_ = static_cast<uint64_t>(
        std::chrono::duration_cast<std::chrono::nanoseconds>(
            now.time_since_epoch()).count());
    rng_state_ ^= static_cast<uint64_t>(bet_amount * 1000000.0);
    rng_state_ ^= static_cast<uint64_t>(mine_count) << 32;
    if (rng_state_ == 0) rng_state_ = 0xDEADBEEFULL;

    for (int i = 0; i < GRID_SIZE; i++) {
        tile_state_[i] = 0;
    }

    shuffle_mines();
    return true;
}

int MinesGame::reveal(int index) {
    if (state_ != MINES_ACTIVE) return -1;
    if (index < 0 || index >= GRID_SIZE) return -1;
    if (tile_state_[index] != 0) return -1; // already revealed

    if (mines_[index]) {
        // Hit a mine
        tile_state_[index] = 2;
        state_ = MINES_LOST;
        // Reveal all mines
        for (int i = 0; i < GRID_SIZE; i++) {
            if (mines_[i] && tile_state_[i] == 0) {
                tile_state_[i] = 2;
            }
        }
        return 2;
    }

    // Safe tile
    double step = fair_step(safe_revealed_);
    fair_multiplier_ *= step;
    safe_revealed_++;
    tile_state_[index] = 1;

    // Check if all safe tiles found → auto-win
    if (safe_revealed_ >= GRID_SIZE - mine_count_) {
        state_ = MINES_WON;
    }

    return 1;
}

double MinesGame::cash_out(double& coins) {
    if (state_ != MINES_ACTIVE && state_ != MINES_WON) return 0.0;
    double winnings = bet_ * fair_multiplier_ * HOUSE_EDGE_FACTOR;
    coins += winnings;

    // Reveal all mines
    for (int i = 0; i < GRID_SIZE; i++) {
        if (mines_[i] && tile_state_[i] == 0) {
            tile_state_[i] = 2;
        }
    }

    state_ = MINES_WON;
    return winnings;
}

double MinesGame::get_multiplier() const {
    if (safe_revealed_ == 0) return 1.0;
    return fair_multiplier_ * HOUSE_EDGE_FACTOR;
}

double MinesGame::get_next_multiplier() const {
    if (state_ != MINES_ACTIVE) return 0.0;
    if (safe_revealed_ >= GRID_SIZE - mine_count_) return 0.0;
    double next = fair_multiplier_ * fair_step(safe_revealed_);
    return next * HOUSE_EDGE_FACTOR;
}

double MinesGame::get_potential_win() const {
    return bet_ * get_multiplier();
}

int MinesGame::get_tile_state(int index) const {
    if (index < 0 || index >= GRID_SIZE) return -1;
    return tile_state_[index];
}
