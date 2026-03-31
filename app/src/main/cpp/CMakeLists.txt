cmake_minimum_required(VERSION 3.22.1)
project("bitclickempire")

add_library(${CMAKE_PROJECT_NAME} SHARED
    native_bridge.cpp
    game_engine.cpp
    game_state.cpp
    upgrade.cpp
    upgrade_data.cpp
    project.cpp
    project_data.cpp
    boost.cpp
    boost_data.cpp
    mines_game.cpp
    blackjack_game.cpp
)

target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    log
)
