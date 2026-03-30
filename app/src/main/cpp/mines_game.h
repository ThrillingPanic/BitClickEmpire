#pragma once

#include <cstdint>

enum MinesState {
    MINES_INACTIVE = 0,
    MINES_ACTIVE = 1,
    MINES_WON = 2,
    MINES_LOST = 3
};

class MinesGame {
public:
    static const int GRID_SIZE = 25; // 5x5
    static constexpr double HOUSE_EDGE_FACTOR = 0.99; // 1% house edge on payout

    MinesGame();

    bool start(int mine_count, double bet_amount, double& coins);
    int reveal(int index);        // returns: -1=invalid, 1=safe, 2=mine
    double cash_out(double& coins); // returns winnings added to coins

    MinesState get_state() const { return state_; }
    int get_mine_count() const { return mine_count_; }
    double get_bet() const { return bet_; }
    double get_multiplier() const;
    double get_next_multiplier() const;
    double get_potential_win() const;
    int get_tiles_revealed() const { return safe_revealed_; }
    int get_tile_state(int index) const;

private:
    MinesState state_;
    bool mines_[GRID_SIZE];
    int tile_state_[GRID_SIZE]; // 0=hidden, 1=safe-revealed, 2=mine-revealed
    int mine_count_;
    int safe_revealed_;
    double bet_;
    double fair_multiplier_; // accumulated true odds multiplier (no edge)
    uint64_t rng_state_;

    uint64_t next_rng();
    void shuffle_mines();
    double fair_step(int step) const;
};
