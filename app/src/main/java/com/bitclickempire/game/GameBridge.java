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
    public native String nativeSave();
    public native boolean nativeLoad(String data);
}
