package com.bitclickempire.game;

import android.app.AlertDialog;
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
import android.widget.LinearLayout;
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
    private BitcoinParticleView particleView;
    private RecyclerView rvUpgrades;
    private RecyclerView rvProjects;
    private RecyclerView rvActiveProjects;
    private RecyclerView rvBoosts;
    private View tabClick;
    private View tabUpgrades;
    private View tabProjects;
    private View tabBoosts;
    private TextView navClick;
    private TextView navUpgrades;
    private TextView navProjects;
    private TextView navBoosts;
    private TextView tvWorkers;
    private TextView tvActiveHeader;
    private Button btnHire;

    private UpgradeAdapter upgradeAdapter;
    private ProjectAdapter projectAdapter;
    private ActiveProjectAdapter activeProjectAdapter;
    private BoostAdapter boostAdapter;

    private static final int TAB_CLICK = 0;
    private static final int TAB_UPGRADES = 1;
    private static final int TAB_PROJECTS = 2;
    private static final int TAB_BOOSTS = 3;
    private int currentTab = TAB_CLICK;

    // Worker role ordinals matching C++ WorkerRole enum
    private static final int ROLE_PM = 0;
    private static final int ROLE_ENGINEER = 1;
    private static final int ROLE_DESIGNER = 2;
    private static final int ROLE_ANALYST = 3;
    private static final int ROLE_HACKER = 4;
    private static final String[] ROLE_NAMES = {"Project Manager", "Engineer", "Designer", "Analyst", "Hacker"};
    private static final double[] HIRE_COSTS = {500, 200, 300, 350, 450};

    private static final long TICK_MS = 50;
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
        particleView = findViewById(R.id.particle_view);

        // Upgrades tab
        rvUpgrades = findViewById(R.id.rv_upgrades);
        rvUpgrades.setLayoutManager(new LinearLayoutManager(this));
        upgradeAdapter = new UpgradeAdapter();
        rvUpgrades.setAdapter(upgradeAdapter);

        // Projects tab
        rvProjects = findViewById(R.id.rv_projects);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        projectAdapter = new ProjectAdapter();
        rvProjects.setAdapter(projectAdapter);

        rvActiveProjects = findViewById(R.id.rv_active_projects);
        rvActiveProjects.setLayoutManager(new LinearLayoutManager(this));
        activeProjectAdapter = new ActiveProjectAdapter();
        rvActiveProjects.setAdapter(activeProjectAdapter);

        tvWorkers = findViewById(R.id.tv_workers);
        tvActiveHeader = findViewById(R.id.tv_active_header);
        btnHire = findViewById(R.id.btn_hire);
        btnHire.setOnClickListener(v -> showHireDialog());

        // Boosts tab
        rvBoosts = findViewById(R.id.rv_boosts);
        rvBoosts.setLayoutManager(new LinearLayoutManager(this));
        boostAdapter = new BoostAdapter();
        rvBoosts.setAdapter(boostAdapter);

        // Tab navigation
        tabClick = findViewById(R.id.tab_click);
        tabUpgrades = findViewById(R.id.tab_upgrades);
        tabProjects = findViewById(R.id.tab_projects);
        tabBoosts = findViewById(R.id.tab_boosts);
        navClick = findViewById(R.id.nav_click);
        navUpgrades = findViewById(R.id.nav_upgrades);
        navProjects = findViewById(R.id.nav_projects);
        navBoosts = findViewById(R.id.nav_boosts);

        navClick.setOnClickListener(v -> switchTab(TAB_CLICK));
        navUpgrades.setOnClickListener(v -> switchTab(TAB_UPGRADES));
        navProjects.setOnClickListener(v -> switchTab(TAB_PROJECTS));
        navBoosts.setOnClickListener(v -> switchTab(TAB_BOOSTS));

        btnBitcoin.setOnClickListener(v -> {
            bridge.nativeClick();
            animateClick(v);
            // Spawn particles from the center of the bitcoin button
            float cx = v.getX() + v.getWidth() / 2f;
            float cy = v.getY() + v.getHeight() / 2f;
            particleView.spawnParticles(cx, cy);
            updateUI();
        });

        loadGame();

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

    private void switchTab(int tab) {
        currentTab = tab;
        tabClick.setVisibility(tab == TAB_CLICK ? View.VISIBLE : View.GONE);
        tabUpgrades.setVisibility(tab == TAB_UPGRADES ? View.VISIBLE : View.GONE);
        tabProjects.setVisibility(tab == TAB_PROJECTS ? View.VISIBLE : View.GONE);
        tabBoosts.setVisibility(tab == TAB_BOOSTS ? View.VISIBLE : View.GONE);

        setNavStyle(navClick, tab == TAB_CLICK);
        setNavStyle(navUpgrades, tab == TAB_UPGRADES);
        setNavStyle(navProjects, tab == TAB_PROJECTS);
        setNavStyle(navBoosts, tab == TAB_BOOSTS);

        if (tab == TAB_UPGRADES) {
            upgradeAdapter.notifyDataSetChanged();
        } else if (tab == TAB_PROJECTS) {
            refreshProjectsTab();
        } else if (tab == TAB_BOOSTS) {
            boostAdapter.notifyDataSetChanged();
        }
    }

    private void setNavStyle(TextView nav, boolean active) {
        nav.setTextColor(active ? 0xFFF7931A : 0x88FFFFFF);
        nav.setTypeface(null, active ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void refreshProjectsTab() {
        updateWorkerDisplay();
        projectAdapter.notifyDataSetChanged();
        activeProjectAdapter.notifyDataSetChanged();
        int active = bridge.nativeGetActiveProjectCount();
        tvActiveHeader.setVisibility(active > 0 ? View.VISIBLE : View.GONE);
    }

    private void updateWorkerDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ROLE_NAMES.length; i++) {
            int avail = bridge.nativeGetWorkerAvailable(i);
            int total = bridge.nativeGetWorkerTotal(i);
            if (total > 0) {
                if (sb.length() > 0) sb.append("  |  ");
                sb.append(ROLE_NAMES[i]).append(": ").append(avail).append("/").append(total);
            }
        }
        if (sb.length() == 0) sb.append("No staff hired yet");
        tvWorkers.setText(sb.toString());
    }

    private void updateUI() {
        double coins = bridge.nativeGetCoins();
        tvCoins.setText(formatNumber(coins) + " BTC");
        tvPerSecond.setText(formatNumber(bridge.nativeGetIncomePerSecond()) + " BTC/sec");
        tvClickPower.setText("+" + formatNumber(bridge.nativeGetClickPower()) + " per click");

        if (currentTab == TAB_UPGRADES && rvUpgrades != null) {
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

        if (currentTab == TAB_BOOSTS && rvBoosts != null) {
            for (int i = 0; i < rvBoosts.getChildCount(); i++) {
                View child = rvBoosts.getChildAt(i);
                RecyclerView.ViewHolder vh = rvBoosts.getChildViewHolder(child);
                if (vh instanceof BoostAdapter.VH) {
                    BoostAdapter.VH holder = (BoostAdapter.VH) vh;
                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        boostAdapter.updateVH(holder, position, coins);
                    }
                }
            }
        }

        if (currentTab == TAB_PROJECTS) {
            updateWorkerDisplay();
            // Update active project timers
            if (rvActiveProjects != null) {
                for (int i = 0; i < rvActiveProjects.getChildCount(); i++) {
                    View child = rvActiveProjects.getChildAt(i);
                    RecyclerView.ViewHolder vh = rvActiveProjects.getChildViewHolder(child);
                    if (vh instanceof ActiveProjectAdapter.VH) {
                        ActiveProjectAdapter.VH holder = (ActiveProjectAdapter.VH) vh;
                        int position = holder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            activeProjectAdapter.updateVH(holder, position);
                        }
                    }
                }
            }
            // Refresh active list if completion state changed
            int activeCount = bridge.nativeGetActiveProjectCount();
            tvActiveHeader.setVisibility(activeCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void showHireDialog() {
        String[] items = new String[ROLE_NAMES.length];
        for (int i = 0; i < ROLE_NAMES.length; i++) {
            int total = bridge.nativeGetWorkerTotal(i);
            items[i] = ROLE_NAMES[i] + " (have " + total + ") — " + formatNumber(HIRE_COSTS[i]) + " BTC";
        }
        new AlertDialog.Builder(this, R.style.Theme_BitClickEmpire)
            .setTitle("Hire Staff")
            .setItems(items, (dialog, which) -> {
                bridge.nativeHireWorker(which, HIRE_COSTS[which]);
                refreshProjectsTab();
                updateUI();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showStartProjectDialog(int projectIndex) {
        int roleCount = bridge.nativeGetProjectRequiredRoleCount(projectIndex);
        int[] assignedRoles = new int[roleCount];
        int[] assignedCounts = new int[roleCount];

        // Pre-fill with minimum requirements
        for (int i = 0; i < roleCount; i++) {
            String roleName = bridge.nativeGetProjectRequiredRoleName(projectIndex, i);
            int required = bridge.nativeGetProjectRequiredRoleAmount(projectIndex, i);
            assignedRoles[i] = roleNameToOrdinal(roleName);
            assignedCounts[i] = required;
        }

        // Check if player has enough workers
        boolean canStart = true;
        StringBuilder info = new StringBuilder();
        info.append(bridge.nativeGetProjectName(projectIndex)).append("\n\n");
        info.append("Staff required:\n");
        for (int i = 0; i < roleCount; i++) {
            int avail = bridge.nativeGetWorkerAvailable(assignedRoles[i]);
            String roleName = bridge.nativeGetProjectRequiredRoleName(projectIndex, i);
            int required = assignedCounts[i];
            info.append("  ").append(roleName).append(": ")
                .append(required).append(" (").append(avail).append(" available)\n");
            if (avail < required) canStart = false;
        }
        info.append("\nReward: ").append(formatNumber(bridge.nativeGetProjectReward(projectIndex))).append(" BTC");
        info.append("\nTime: ").append(formatTime(bridge.nativeGetProjectBaseTime(projectIndex)));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_BitClickEmpire)
            .setTitle("Start Project")
            .setMessage(info.toString())
            .setNegativeButton("Cancel", null);

        if (canStart) {
            builder.setPositiveButton("Start", (dialog, which) -> {
                bridge.nativeStartProject(projectIndex, assignedRoles, assignedCounts, roleCount);
                refreshProjectsTab();
                updateUI();
            });
        }

        builder.show();
    }

    private int roleNameToOrdinal(String name) {
        for (int i = 0; i < ROLE_NAMES.length; i++) {
            if (ROLE_NAMES[i].equals(name)) return i;
        }
        return ROLE_ENGINEER;
    }

    static String formatTime(double seconds) {
        int s = (int) seconds;
        if (s < 60) return s + "s";
        if (s < 3600) return (s / 60) + "m " + (s % 60) + "s";
        return (s / 3600) + "h " + ((s % 3600) / 60) + "m";
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

    // RecyclerView adapter for available projects
    private class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
            VH holder = new VH(v);
            holder.btnStart.setOnClickListener(view -> {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    showStartProjectDialog(position);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            String name = bridge.nativeGetProjectName(position);
            String desc = bridge.nativeGetProjectDesc(position);
            double baseTime = bridge.nativeGetProjectBaseTime(position);
            double reward = bridge.nativeGetProjectReward(position);
            int difficulty = bridge.nativeGetProjectDifficulty(position);

            holder.tvName.setText(name);
            holder.tvDesc.setText(desc);
            holder.tvTime.setText("⏱ " + formatTime(baseTime));
            holder.tvReward.setText("💰 " + formatNumber(reward) + " BTC");

            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < difficulty; i++) stars.append("★");
            for (int i = difficulty; i < 5; i++) stars.append("☆");
            holder.tvDifficulty.setText(stars.toString());

            // Build roles string
            int roleCount = bridge.nativeGetProjectRequiredRoleCount(position);
            StringBuilder roles = new StringBuilder("Staff: ");
            for (int i = 0; i < roleCount; i++) {
                if (i > 0) roles.append(", ");
                int amount = bridge.nativeGetProjectRequiredRoleAmount(position, i);
                String roleName = bridge.nativeGetProjectRequiredRoleName(position, i);
                roles.append(amount).append("× ").append(roleName);
            }
            holder.tvRoles.setText(roles.toString());

            // Check if player can start (has enough workers)
            boolean canStart = true;
            for (int i = 0; i < roleCount; i++) {
                String roleName = bridge.nativeGetProjectRequiredRoleName(position, i);
                int required = bridge.nativeGetProjectRequiredRoleAmount(position, i);
                int avail = bridge.nativeGetWorkerAvailable(roleNameToOrdinal(roleName));
                if (avail < required) { canStart = false; break; }
            }
            holder.btnStart.setEnabled(canStart);
            holder.btnStart.setAlpha(canStart ? 1.0f : 0.5f);
        }

        @Override
        public int getItemCount() {
            return bridge.nativeGetProjectCount();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvDesc, tvTime, tvReward, tvDifficulty, tvRoles;
            Button btnStart;

            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_project_name);
                tvDesc = v.findViewById(R.id.tv_project_desc);
                tvTime = v.findViewById(R.id.tv_project_time);
                tvReward = v.findViewById(R.id.tv_project_reward);
                tvDifficulty = v.findViewById(R.id.tv_project_difficulty);
                tvRoles = v.findViewById(R.id.tv_project_roles);
                btnStart = v.findViewById(R.id.btn_start_project);
            }
        }
    }

    // RecyclerView adapter for active (in-progress / completed) projects
    private class ActiveProjectAdapter extends RecyclerView.Adapter<ActiveProjectAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_active_project, parent, false);
            VH holder = new VH(v);
            holder.btnClaim.setOnClickListener(view -> {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    bridge.nativeClaimProject(position);
                    notifyDataSetChanged();
                    refreshProjectsTab();
                    updateUI();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            updateVH(holder, position);
        }

        void updateVH(VH holder, int position) {
            String name = bridge.nativeGetActiveProjectName(position);
            double progress = bridge.nativeGetActiveProjectProgress(position);
            double remaining = bridge.nativeGetActiveProjectRemaining(position);
            boolean completed = bridge.nativeIsActiveProjectCompleted(position);

            holder.tvName.setText(name);

            int pct = (int) (progress * 100);

            // Update progress bar width
            ViewGroup.LayoutParams params = holder.progressFill.getLayoutParams();
            ViewGroup parent = (ViewGroup) holder.progressFill.getParent();
            if (parent.getWidth() > 0) {
                params.width = (int) (parent.getWidth() * progress);
                holder.progressFill.setLayoutParams(params);
            }

            holder.tvProgressPct.setText(pct + "%");

            if (completed) {
                holder.tvTimer.setText("COMPLETE!");
                holder.tvTimer.setTextColor(0xFF4EC9B0);
                holder.btnClaim.setVisibility(View.VISIBLE);
                holder.progressFill.setBackgroundColor(0xFFF7931A);
            } else {
                holder.tvTimer.setText("⏱ " + formatTime(remaining) + " remaining");
                holder.tvTimer.setTextColor(0xFFF7931A);
                holder.btnClaim.setVisibility(View.GONE);
                holder.progressFill.setBackgroundColor(0xFF4EC9B0);
            }
        }

        @Override
        public int getItemCount() {
            return bridge.nativeGetActiveProjectCount();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvTimer, tvProgressPct;
            View progressFill;
            Button btnClaim;

            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_active_name);
                tvTimer = v.findViewById(R.id.tv_active_timer);
                tvProgressPct = v.findViewById(R.id.tv_progress_pct);
                progressFill = v.findViewById(R.id.progress_fill);
                btnClaim = v.findViewById(R.id.btn_claim);
            }
        }
    }

    // RecyclerView adapter for boosts
    private class BoostAdapter extends RecyclerView.Adapter<BoostAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_boost, parent, false);
            VH holder = new VH(v);
            holder.btnBuy.setOnClickListener(view -> {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    bridge.nativeBuyBoost(position);
                    notifyItemChanged(position);
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
            String name = bridge.nativeGetBoostName(position);
            String desc = bridge.nativeGetBoostDesc(position);
            double cost = bridge.nativeGetBoostCost(position);
            int level = bridge.nativeGetBoostLevel(position);
            int maxLevel = bridge.nativeGetBoostMaxLevel(position);
            String effect = bridge.nativeGetBoostEffect(position);

            holder.tvName.setText(name);
            holder.tvDesc.setText(desc);
            holder.tvLevel.setText("Lv." + level + "/" + maxLevel);
            holder.tvEffect.setText(effect);

            if (level >= maxLevel) {
                holder.tvCost.setText("MAXED");
                holder.tvCost.setTextColor(0xFF4EC9B0);
                holder.btnBuy.setEnabled(false);
                holder.btnBuy.setAlpha(0.4f);
                holder.btnBuy.setText("MAX");
            } else {
                holder.tvCost.setText("Cost: " + formatNumber(cost) + " BTC");
                holder.tvCost.setTextColor(0xFFF7931A);
                holder.btnBuy.setEnabled(coins >= cost);
                holder.btnBuy.setAlpha(coins >= cost ? 1.0f : 0.5f);
                holder.btnBuy.setText("BUY");
            }
        }

        @Override
        public int getItemCount() {
            return bridge.nativeGetBoostCount();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvDesc, tvLevel, tvEffect, tvCost;
            Button btnBuy;

            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_boost_name);
                tvDesc = v.findViewById(R.id.tv_boost_desc);
                tvLevel = v.findViewById(R.id.tv_boost_level);
                tvEffect = v.findViewById(R.id.tv_boost_effect);
                tvCost = v.findViewById(R.id.tv_boost_cost);
                btnBuy = v.findViewById(R.id.btn_buy_boost);
            }
        }
    }
}
