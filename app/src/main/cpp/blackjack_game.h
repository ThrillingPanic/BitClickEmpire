#pragma once

#include <cstdint>
#include <vector>

enum BlackjackState {
    BJ_INACTIVE = 0,
    BJ_ACTIVE = 1,
    BJ_WON = 2,
    BJ_LOST = 3,
    BJ_PUSH = 4  // Tie
};

enum CardSuit {
    SUIT_HEARTS,
    SUIT_DIAMONDS,
    SUIT_CLUBS,
    SUIT_SPADES
};

struct Card {
    int rank;    // 1-13 (Ace through King)
    CardSuit suit;
    
    Card(int r = 0, CardSuit s = SUIT_HEARTS) : rank(r), suit(s) {}
};

class BlackjackGame {
public:
    BlackjackGame();

    bool start(double bet_amount, double& coins);
    bool hit();                      // returns: false if bust
    bool stand();                    // returns: true if game complete
    double finish_game(double& coins); // returns winnings added to coins

    BlackjackState get_state() const { return state_; }
    double get_bet() const { return bet_; }
    int get_player_hand_value() const;
    int get_dealer_hand_value() const;
    double get_potential_win() const;
    
    // Card access for UI
    int get_player_card_count() const { return static_cast<int>(player_hand_.size()); }
    int get_dealer_visible_card_count() const; // Only shows one until stand
    Card get_player_card(int index) const;
    Card get_dealer_card(int index) const;
    bool is_dealer_hand_hidden() const { return state_ == BJ_ACTIVE; }

private:
    BlackjackState state_;
    std::vector<Card> shoe_;          // Deck of cards
    std::vector<Card> player_hand_;
    std::vector<Card> dealer_hand_;
    double bet_;
    uint64_t rng_state_;
    bool player_stood_;

    void initialize_shoe();
    void shuffle_shoe();
    Card deal_card();
    uint64_t next_rng();
    int calculate_hand_value(const std::vector<Card>& hand) const;
    bool is_bust(int hand_value) const;
    bool is_blackjack(const std::vector<Card>& hand) const;
};
