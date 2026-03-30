package com.bitclickempire.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private GameBridge bridge;
    private Handler handler;
    private Runnable gameLoop;

    private TextView tvCoins;
    private TextView tvPerSecond;
    private TextView tvClickPower;
    private View btnBitcoin;
    private RecyclerView rvUpgrades;

    private UpgradeAdapter upgradeAdapter;

    private static final long TICK_MS = 50; // 20 FPS update
    private static final String PREFS_NAME = "bitclick_save";
    private static final String SAVE_KEY = "game_state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bridge = new GameBridge();
        bridge.nativeInit();

        tvCoins = findViewById(R.id.tv_coins);
        tvPerSecond = findViewById(R.id.tv_per_second);
        tvClickPower = findViewById(R.id.tv_click_power);
        btnBitcoin = findViewById(R.id.btn_bitcoin);

        rvUpgrades = findViewById(R.id.rv_upgrades);
        rvUpgrades.setLayoutManager(new LinearLayoutManager(this));
        upgradeAdapter = new UpgradeAdapter();
        rvUpgrades.setAdapter(upgradeAdapter);

        btnBitcoin.setOnClickListener(v -> {
            bridge.nativeClick();
            animateClick(v);
            updateUI();
        });

        // Load saved game
        loadGame();

        // Start game loop
        handler = new Handler(Looper.getMainLooper());
        gameLoop = new Runnable() {
            @Override
            public void run() {
                bridge.nativeUpdate();
                updateUI();
                handler.postDelayed(this, TICK_MS);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(gameLoop);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(gameLoop);
        saveGame();
    }

    private void animateClick(View v) {
        ScaleAnimation anim = new ScaleAnimation(
            1.0f, 0.9f, 1.0f, 0.9f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        );
        anim.setDuration(80);
        anim.setRepeatCount(1);
        anim.setRepeatMode(ScaleAnimation.REVERSE);
        v.startAnimation(anim);
    }

    private void updateUI() {
        double coins = bridge.nativeGetCoins();
        tvCoins.setText(formatNumber(coins) + " BTC");
        tvPerSecond.setText(formatNumber(bridge.nativeGetIncomePerSecond()) + " BTC/sec");
        tvClickPower.setText("+" + formatNumber(bridge.nativeGetClickPower()) + " per click");

        // Update visible items directly to avoid notifyDataSetChanged spam which can break click events
        if (rvUpgrades != null) {
            for (int i = 0; i < rvUpgrades.getChildCount(); i++) {
                View child = rvUpgrades.getChildAt(i);
                RecyclerView.ViewHolder vh = rvUpgrades.getChildViewHolder(child);
                if (vh instanceof UpgradeAdapter.VH) {
                    UpgradeAdapter.VH holder = (UpgradeAdapter.VH) vh;
                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        upgradeAdapter.updateVH(holder, position, coins);
                    }
                }
            }
        }
    }

    private void saveGame() {
        String data = bridge.nativeSave();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(SAVE_KEY, data).apply();
    }

    private void loadGame() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String data = prefs.getString(SAVE_KEY, null);
        if (data != null) {
            bridge.nativeLoad(data);
        }
    }

    static String formatNumber(double num) {
        if (num < 1000) {
            return new DecimalFormat("#,##0.0").format(num);
        }
        String[] suffixes = {"", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc"};
        int tier = 0;
        double scaled = num;
        while (scaled >= 1000 && tier < suffixes.length - 1) {
            scaled /= 1000;
            tier++;
        }
        return new DecimalFormat("#,##0.00").format(scaled) + suffixes[tier];
    }

    // RecyclerView adapter for upgrades
    private class UpgradeAdapter extends RecyclerView.Adapter<UpgradeAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upgrade, parent, false);
            VH holder = new VH(v);
            holder.btnBuy.setOnClickListener(view -> {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    bridge.nativeBuyUpgrade(position);
                    updateUI();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            double coins = bridge.nativeGetCoins();
            updateVH(holder, position, coins);
        }

        void updateVH(VH holder, int position, double coins) {
            String name = bridge.nativeGetUpgradeName(position);
            String desc = bridge.nativeGetUpgradeDesc(position);
            double cost = bridge.nativeGetUpgradeCost(position);
            double income = bridge.nativeGetUpgradeIncome(position);
            int owned = bridge.nativeGetUpgradeOwned(position);

            holder.tvName.setText(name + " (" + owned + ")");
            holder.tvDesc.setText(desc);
            holder.tvCost.setText("Cost: " + formatNumber(cost) + " BTC");
            holder.tvIncome.setText("Earning: " + formatNumber(income) + " BTC/sec");
            holder.btnBuy.setEnabled(coins >= cost);
            holder.btnBuy.setAlpha(coins >= cost ? 1.0f : 0.5f);
        }

        @Override
        public int getItemCount() {
            return bridge.nativeGetUpgradeCount();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvDesc, tvCost, tvIncome;
            Button btnBuy;

            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_upgrade_name);
                tvDesc = v.findViewById(R.id.tv_upgrade_desc);
                tvCost = v.findViewById(R.id.tv_upgrade_cost);
                tvIncome = v.findViewById(R.id.tv_upgrade_income);
                btnBuy = v.findViewById(R.id.btn_buy);
            }
        }
    }
}
