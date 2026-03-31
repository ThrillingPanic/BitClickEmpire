#include "blackjack_game.h"
#include <chrono>
#include <algorithm>

BlackjackGame::BlackjackGame()
    : state_(BJ_INACTIVE)
    , bet_(0)
    , rng_state_(1)
    , player_stood_(false)
{
}

void BlackjackGame::initialize_shoe() {
    shoe_.clear();
    // Standard 1-deck shoe: 4 suits × 13 ranks
    for (int suit = 0; suit < 4; suit++) {
        for (int rank = 1; rank <= 13; rank++) {
            shoe_.emplace_back(rank, static_cast<CardSuit>(suit));
        }
    }
    shuffle_shoe();
}

uint64_t BlackjackGame::next_rng() {
    rng_state_ ^= rng_state_ << 13;
    rng_state_ ^= rng_state_ >> 7;
    rng_state_ ^= rng_state_ << 17;
    return rng_state_;
}

void BlackjackGame::shuffle_shoe() {
    if (shoe_.size() < 10) {
        initialize_shoe();
    }
    // Fisher-Yates shuffle
    for (int i = static_cast<int>(shoe_.size()) - 1; i > 0; i--) {
        int j = static_cast<int>(next_rng() % static_cast<uint64_t>(i + 1));
        std::swap(shoe_[i], shoe_[j]);
    }
}

Card BlackjackGame::deal_card() {
    if (shoe_.empty()) {
        initialize_shoe();
    }
    Card card = shoe_.back();
    shoe_.pop_back();
    return card;
}

int BlackjackGame::calculate_hand_value(const std::vector<Card>& hand) const {
    int value = 0;
    int aces = 0;

    for (const auto& card : hand) {
        if (card.rank == 1) {
            aces++;
            value += 11;
        } else if (card.rank >= 11) {
            value += 10;  // Jack, Queen, King
        } else {
            value += card.rank;
        }
    }

    // Adjust for aces if busting
    while (value > 21 && aces > 0) {
        value -= 10;
        aces--;
    }

    return value;
}

bool BlackjackGame::is_bust(int hand_value) const {
    return hand_value > 21;
}

bool BlackjackGame::is_blackjack(const std::vector<Card>& hand) const {
    return hand.size() == 2 && calculate_hand_value(hand) == 21;
}

bool BlackjackGame::start(double bet_amount, double& coins) {
    if (state_ == BJ_ACTIVE) return false;
    if (bet_amount <= 0.0) return false;
    double max_bet = coins * 0.10;
    if (bet_amount > max_bet + 0.01) return false;  // small epsilon for fp
    if (bet_amount > coins) return false;

    bet_ = bet_amount;
    state_ = BJ_ACTIVE;
    player_stood_ = false;
    player_hand_.clear();
    dealer_hand_.clear();

    coins -= bet_amount;

    // Seed RNG
    auto now = std::chrono::steady_clock::now();
    rng_state_ = static_cast<uint64_t>(
        std::chrono::duration_cast<std::chrono::nanoseconds>(
            now.time_since_epoch()).count());
    rng_state_ ^= static_cast<uint64_t>(bet_amount * 1000000.0);
    if (rng_state_ == 0) rng_state_ = 0xDEADBEEFULL;

    initialize_shoe();

    // Deal initial cards: player gets 2, dealer gets 2 (1 hidden)
    player_hand_.push_back(deal_card());
    dealer_hand_.push_back(deal_card());
    player_hand_.push_back(deal_card());
    dealer_hand_.push_back(deal_card());

    // Check for blackjacks
    int player_value = get_player_hand_value();
    int dealer_value = get_dealer_hand_value();

    if (is_blackjack(player_hand_) && is_blackjack(dealer_hand_)) {
        // Both blackjack = push
        state_ = BJ_PUSH;
        return true;
    }

    if (is_blackjack(player_hand_)) {
        // Player blackjack = auto win with 3:2
        state_ = BJ_WON;
        return true;
    }

    return true;
}

bool BlackjackGame::hit() {
    if (state_ != BJ_ACTIVE) return false;

    player_hand_.push_back(deal_card());
    int value = get_player_hand_value();

    if (is_bust(value)) {
        state_ = BJ_LOST;
        return false;
    }

    return true;
}

bool BlackjackGame::stand() {
    if (state_ != BJ_ACTIVE) return false;

    player_stood_ = true;

    // Dealer plays out hand (must hit on 16, stand on 17+)
    while (true) {
        int dealer_value = get_dealer_hand_value();
        if (dealer_value >= 17) break;
        dealer_hand_.push_back(deal_card());
    }

    // Determine outcome
    int player_value = get_player_hand_value();
    int dealer_value = get_dealer_hand_value();

    if (is_bust(dealer_value)) {
        state_ = BJ_WON;
    } else if (player_value > dealer_value) {
        state_ = BJ_WON;
    } else if (player_value < dealer_value) {
        state_ = BJ_LOST;
    } else {
        state_ = BJ_PUSH;
    }

    return true;
}

double BlackjackGame::finish_game(double& coins) {
    if (state_ == BJ_INACTIVE) return 0.0;

    double winnings = 0.0;
    constexpr double HOUSE_EDGE = 0.98;  // 2% house edge

    if (state_ == BJ_WON) {
        // Check for blackjack (3:2 payout)
        if (is_blackjack(player_hand_)) {
            winnings = bet_ * 2.5 * HOUSE_EDGE;  // 3:2 = 2.5x bet
        } else {
            winnings = bet_ * 2.0 * HOUSE_EDGE;  // Win = 2x bet (get original + winnings)
        }
        coins += winnings;
    } else if (state_ == BJ_PUSH) {
        winnings = bet_;  // Return original bet
        coins += winnings;
    }
    // BJ_LOST: no winnings, bet already deducted

    state_ = BJ_INACTIVE;
    return winnings;
}

int BlackjackGame::get_player_hand_value() const {
    return calculate_hand_value(player_hand_);
}

int BlackjackGame::get_dealer_hand_value() const {
    if (player_stood_) {
        return calculate_hand_value(dealer_hand_);
    } else {
        // Only reveal first card until player stands
        if (dealer_hand_.empty()) return 0;
        if (dealer_hand_[0].rank == 1) return 11;
        if (dealer_hand_[0].rank >= 11) return 10;
        return dealer_hand_[0].rank;
    }
}

double BlackjackGame::get_potential_win() const {
    if (state_ != BJ_ACTIVE) return 0.0;
    int value = get_player_hand_value();
    if (is_bust(value)) return 0.0;
    
    // Rough estimate: if standing now
    if (is_blackjack(player_hand_)) {
        return bet_ * 2.5 * 0.98;
    } else {
        return bet_ * 2.0 * 0.98;
    }
}

int BlackjackGame::get_player_card_count() const {
    return static_cast<int>(player_hand_.size());
}

int BlackjackGame::get_dealer_visible_card_count() const {
    if (player_stood_) {
        return static_cast<int>(dealer_hand_.size());
    } else {
        return 1;  // Only show first card
    }
}

Card BlackjackGame::get_player_card(int index) const {
    if (index >= 0 && index < static_cast<int>(player_hand_.size())) {
        return player_hand_[index];
    }
    return Card(0, SUIT_HEARTS);
}

Card BlackjackGame::get_dealer_card(int index) const {
    if (index >= 0 && index < static_cast<int>(dealer_hand_.size())) {
        return dealer_hand_[index];
    }
    return Card(0, SUIT_HEARTS);
}
