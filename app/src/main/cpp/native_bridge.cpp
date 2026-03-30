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

// === Project System JNI ===

JNIEXPORT jint JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetProjectCount(JNIEnv*, jobject) {
    return engine().get_project_count();
}

JNIEXPORT jstring JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetProjectName(JNIEnv* env, jobject, jint index) {
    return env->NewStringUTF(engine().get_project_name(index).c_str());
}

JNIEXPORT jstring JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetProjectDesc(JNIEnv* env, jobject, jint index) {
    return env->NewStringUTF(engine().get_project_desc(index).c_str());
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetProjectBaseTime(JNIEnv*, jobject, jint index) {
    return engine().get_project_base_time(index);
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetProjectReward(JNIEnv*, jobject, jint index) {
    return engine().get_project_reward(index);
}

JNIEXPORT jint JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetProjectDifficulty(JNIEnv*, jobject, jint index) {
    return engine().get_project_difficulty(index);
}

JNIEXPORT jint JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetProjectRequiredRoleCount(JNIEnv*, jobject, jint index) {
    return engine().get_project_required_role_count(index);
}

JNIEXPORT jstring JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetProjectRequiredRoleName(JNIEnv* env, jobject, jint projIndex, jint roleIndex) {
    return env->NewStringUTF(engine().get_project_required_role_name(projIndex, roleIndex).c_str());
}

JNIEXPORT jint JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetProjectRequiredRoleAmount(JNIEnv*, jobject, jint projIndex, jint roleIndex) {
    return engine().get_project_required_role_amount(projIndex, roleIndex);
}

JNIEXPORT jint JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetActiveProjectCount(JNIEnv*, jobject) {
    return engine().get_active_project_count();
}

JNIEXPORT jstring JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetActiveProjectName(JNIEnv* env, jobject, jint index) {
    return env->NewStringUTF(engine().get_active_project_name(index).c_str());
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetActiveProjectProgress(JNIEnv*, jobject, jint index) {
    return engine().get_active_project_progress(index);
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetActiveProjectRemaining(JNIEnv*, jobject, jint index) {
    return engine().get_active_project_remaining(index);
}

JNIEXPORT jboolean JNICALL
Java_com_bitclickempire_game_GameBridge_nativeIsActiveProjectCompleted(JNIEnv*, jobject, jint index) {
    return engine().is_active_project_completed(index);
}

JNIEXPORT jboolean JNICALL
Java_com_bitclickempire_game_GameBridge_nativeStartProject(JNIEnv* env, jobject, jint projectIndex,
        jintArray roles, jintArray counts, jint assignmentSize) {
    std::vector<AssignedWorkers> assignment;
    jint* roleArr = env->GetIntArrayElements(roles, nullptr);
    jint* countArr = env->GetIntArrayElements(counts, nullptr);
    for (int i = 0; i < assignmentSize; i++) {
        assignment.push_back({static_cast<WorkerRole>(roleArr[i]), countArr[i]});
    }
    env->ReleaseIntArrayElements(roles, roleArr, 0);
    env->ReleaseIntArrayElements(counts, countArr, 0);
    return engine().start_project(projectIndex, assignment);
}

JNIEXPORT jboolean JNICALL
Java_com_bitclickempire_game_GameBridge_nativeClaimProject(JNIEnv*, jobject, jint activeIndex) {
    return engine().claim_project(activeIndex);
}

JNIEXPORT jint JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetWorkerAvailable(JNIEnv*, jobject, jint role) {
    return engine().get_worker_available(role);
}

JNIEXPORT jint JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetWorkerTotal(JNIEnv*, jobject, jint role) {
    return engine().get_worker_total(role);
}

JNIEXPORT void JNICALL
Java_com_bitclickempire_game_GameBridge_nativeHireWorker(JNIEnv*, jobject, jint role, jdouble cost) {
    engine().hire_worker(role, cost);
}

JNIEXPORT jdouble JNICALL
Java_com_bitclickempire_game_GameBridge_nativeGetGameTime(JNIEnv*, jobject) {
    return engine().get_game_time();
}

} // extern "C"
