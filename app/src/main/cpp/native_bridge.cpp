#include <jni.h>
#include <string>
#include <memory>
#include "game_engine.h"

static std::unique_ptr<GameEngine> g_engine;

static GameEngine& engine() {
    if (!g_engine) {
        g_engine = std::make_unique<GameEngine>();
    }
    return *g_engine;
}

extern "C" {

JNIEXPORT void JNICALL
Java_com_bitclickempire_game_GameBridge_nativeInit(JNIEnv*, jobject) {
    g_engine = std::make_unique<GameEngine>();
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeClick(JNIEnv*, jobject) {
    return engine().on_click();
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeUpdate(JNIEnv*, jobject) {
    return engine().update();
}

JNIEXPORT jboolean JNICALL
Java_com_bitclickempire_game_GameBridge_nativeBuyUpgrade(JNIEnv*, jobject, jint index) {
    return engine().buy_upgrade(index);
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetCoins(JNIEnv*, jobject) {
    return engine().get_coins();
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetTotalEarned(JNIEnv*, jobject) {
    return engine().get_total_earned();
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetIncomePerSecond(JNIEnv*, jobject) {
    return engine().get_income_per_second();
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetClickPower(JNIEnv*, jobject) {
    return engine().get_click_power();
}

JNIEXPORT jint JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetUpgradeCount(JNIEnv*, jobject) {
    return engine().get_upgrade_count();
}

JNIEXPORT jstring JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetUpgradeName(JNIEnv* env, jobject, jint index) {
    return env->NewStringUTF(engine().get_upgrade_name(index).c_str());
}

JNIEXPORT jstring JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetUpgradeDesc(JNIEnv* env, jobject, jint index) {
    return env->NewStringUTF(engine().get_upgrade_desc(index).c_str());
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetUpgradeCost(JNIEnv*, jobject, jint index) {
    return engine().get_upgrade_cost(index);
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetUpgradeIncome(JNIEnv*, jobject, jint index) {
    return engine().get_upgrade_income(index);
}

JNIEXPORT jint JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetUpgradeOwned(JNIEnv*, jobject, jint index) {
    return engine().get_upgrade_owned(index);
}

JNIEXPORT jstring JNICALL
Java_com_bitclickempire_game_GameBridge_nativeSave(JNIEnv* env, jobject) {
    return env->NewStringUTF(engine().save().c_str());
}

JNIEXPORT jboolean JNICALL
Java_com_bitclickempire_game_GameBridge_nativeLoad(JNIEnv* env, jobject, jstring data) {
    const char* str = env->GetStringUTFChars(data, nullptr);
    bool ok = engine().load(std::string(str));
    env->ReleaseStringUTFChars(data, str);
    return ok;
}

} // extern "C"
