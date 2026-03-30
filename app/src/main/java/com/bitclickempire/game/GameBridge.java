package com.bitclickempire.game;

// JNI bridge to the C++ game engine
public class GameBridge {
    static {
        System.loadLibrary("bitclickempire");
    }

    public native void nativeInit();
    public native double nativeClick();
    public native double nativeUpdate();
    public native boolean nativeBuyUpgrade(int index);
    public native double nativeGetCoins();
    public native double nativeGetTotalEarned();
    public native double nativeGetIncomePerSecond();
    public native double nativeGetClickPower();
    public native int nativeGetUpgradeCount();
    public native String nativeGetUpgradeName(int index);
    public native String nativeGetUpgradeDesc(int index);
    public native double nativeGetUpgradeCost(int index);
    public native double nativeGetUpgradeIncome(int index);
    public native int nativeGetUpgradeOwned(int index);
    public native boolean nativeIsUpgradeLocked(int index);
    public native String nativeSave();
    public native boolean nativeLoad(String data);

    // Project system
    public native int nativeGetProjectCount();
    public native String nativeGetProjectName(int index);
    public native String nativeGetProjectDesc(int index);
    public native double nativeGetProjectBaseTime(int index);
    public native double nativeGetProjectReward(int index);
    public native int nativeGetProjectDifficulty(int index);
    public native int nativeGetProjectRequiredRoleCount(int index);
    public native String nativeGetProjectRequiredRoleName(int projIndex, int roleIndex);
    public native int nativeGetProjectRequiredRoleAmount(int projIndex, int roleIndex);
    public native boolean nativeIsProjectLocked(int index);

    // Active projects
    public native int nativeGetActiveProjectCount();
    public native String nativeGetActiveProjectName(int index);
    public native double nativeGetActiveProjectProgress(int index);
    public native double nativeGetActiveProjectRemaining(int index);
    public native boolean nativeIsActiveProjectCompleted(int index);
    public native boolean nativeStartProject(int projectIndex, int[] roles, int[] counts, int assignmentSize);
    public native boolean nativeClaimProject(int activeIndex);

    // Worker pool
    public native int nativeGetWorkerAvailable(int role);
    public native int nativeGetWorkerTotal(int role);
    public native void nativeHireWorker(int role, double cost);
    public native double nativeGetGameTime();

    // Boost system
    public native int nativeGetBoostCount();
    public native String nativeGetBoostName(int index);
    public native String nativeGetBoostDesc(int index);
    public native double nativeGetBoostCost(int index);
    public native int nativeGetBoostLevel(int index);
    public native int nativeGetBoostMaxLevel(int index);
    public native String nativeGetBoostEffect(int index);
    public native boolean nativeBuyBoost(int index);
    public native double nativeGetIdleEarningsPct();
    public native double nativeGetIdleDurationMax();
    public native double nativeGetLastLuckyBonus();

    // Mines gambling
    public native boolean nativeMinesStart(int mineCount, double bet);
    public native int nativeMinesReveal(int index);
    public native double nativeMinesCashOut();
    public native int nativeMinesGetState();
    public native int nativeMinesGetMineCount();
    public native double nativeMinesGetBet();
    public native double nativeMinesGetMultiplier();
    public native double nativeMinesGetNextMultiplier();
    public native double nativeMinesGetPotentialWin();
    public native int nativeMinesGetTilesRevealed();
    public native int nativeMinesGetTileState(int index);
    public native double nativeMinesGetMaxBet();

    // Byte bonus
    public native void nativeSetByteBonus(double multiplier);
    public native void nativeClearByteBonus();
}
