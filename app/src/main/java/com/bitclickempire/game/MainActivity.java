package com.bitclickempire.game;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private ByteFloaterView byteFloater;
    private TextView tvByteBonus;
    private RecyclerView rvUpgrades;
    private RecyclerView rvProjects;
    private RecyclerView rvActiveProjects;
    private RecyclerView rvBoosts;
    private View tabClick;
    private View tabUpgrades;
    private View tabProjects;
    private View tabBoosts;
    private View tabCasino;
    private TextView navClick;
    private TextView navUpgrades;
    private TextView navProjects;
    private TextView navBoosts;
    private TextView navCasino;
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
    private static final int TAB_CASINO = 4;
    private int currentTab = TAB_CLICK;

    // Worker role ordinals matching C++ WorkerRole enum
    private static final int ROLE_PM = 0;
    private static final int ROLE_ENGINEER = 1;
    private static final int ROLE_DESIGNER = 2;
    private static final int ROLE_ANALYST = 3;
    private static final int ROLE_HACKER = 4;
    private static final String[] ROLE_NAMES = {"Project Manager", "Engineer", "Designer", "Analyst", "Hacker"};
    private static final double[] HIRE_COSTS = {500, 200, 300, 350, 450};

    // Mines game UI
    private View minesSetup;
    private View minesGameSection;
    private TextView tvMineCount;
    private EditText etMinesBet;
    private Button btnStartMines;
    private Button btnMinesAction;
    private TextView tvCasinoBalance;
    private TextView tvMinesMultiplier;
    private TextView tvMinesNext;
    private TextView tvMinesPotential;
    private TextView tvMinesInfo;
    private TextView tvMinesResult;
    private LinearLayout minesGridContainer;
    private TextView[] mineTiles;
    private int selectedMineCount = 3;

    private static final long TICK_MS = 50;
    private static final String PREFS_NAME = "bitclick_save";
    private static final String SAVE_KEY = "game_state";

    // Byte collection system
    private int bytesCollected = 0;
    private static final int BYTES_NEEDED = 3;
    private static final double BYTE_BONUS_MULTIPLIER = 5.0;
    private static final long BYTE_BONUS_DURATION_MS = 60_000; // 1 minute
    private boolean byteBonusActive = false;
    private Runnable byteBonusExpiry;

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
        byteFloater = findViewById(R.id.byte_floater);
        tvByteBonus = findViewById(R.id.tv_byte_bonus);

        // Byte collection callback
        byteFloater.setOnByteCaughtListener(this::onByteCaught);

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

        // Casino / Mines setup
        minesSetup = findViewById(R.id.mines_setup);
        minesGameSection = findViewById(R.id.mines_game_section);
        tvMineCount = findViewById(R.id.tv_mine_count);
        etMinesBet = findViewById(R.id.et_mines_bet);
        btnStartMines = findViewById(R.id.btn_start_mines);
        btnMinesAction = findViewById(R.id.btn_mines_action);
        tvCasinoBalance = findViewById(R.id.tv_casino_balance);
        tvMinesMultiplier = findViewById(R.id.tv_mines_multiplier);
        tvMinesNext = findViewById(R.id.tv_mines_next);
        tvMinesPotential = findViewById(R.id.tv_mines_potential);
        tvMinesInfo = findViewById(R.id.tv_mines_info);
        tvMinesResult = findViewById(R.id.tv_mines_result);
        minesGridContainer = findViewById(R.id.mines_grid_container);

        findViewById(R.id.btn_mine_minus).setOnClickListener(v -> {
            if (selectedMineCount > 1) {
                selectedMineCount--;
                tvMineCount.setText(String.valueOf(selectedMineCount));
            }
        });
        findViewById(R.id.btn_mine_plus).setOnClickListener(v -> {
            if (selectedMineCount < 24) {
                selectedMineCount++;
                tvMineCount.setText(String.valueOf(selectedMineCount));
            }
        });
        findViewById(R.id.btn_bet_25).setOnClickListener(v -> {
            double max = bridge.nativeMinesGetMaxBet();
            etMinesBet.setText(String.format("%.1f", max * 0.25));
        });
        findViewById(R.id.btn_bet_50).setOnClickListener(v -> {
            double max = bridge.nativeMinesGetMaxBet();
            etMinesBet.setText(String.format("%.1f", max * 0.50));
        });
        findViewById(R.id.btn_bet_max).setOnClickListener(v -> {
            double max = bridge.nativeMinesGetMaxBet();
            etMinesBet.setText(String.format("%.1f", max));
        });
        btnStartMines.setOnClickListener(v -> startMinesGame());
        btnMinesAction.setOnClickListener(v -> {
            int state = bridge.nativeMinesGetState();
            if (state == 1) {
                int revealed = bridge.nativeMinesGetTilesRevealed();
                if (revealed > 0) cashOutMines();
            } else {
                resetMinesUI();
            }
        });

        // Tab navigation
        tabClick = findViewById(R.id.tab_click);
        tabUpgrades = findViewById(R.id.tab_upgrades);
        tabProjects = findViewById(R.id.tab_projects);
        tabBoosts = findViewById(R.id.tab_boosts);
        tabCasino = findViewById(R.id.tab_casino);
        navClick = findViewById(R.id.nav_click);
        navUpgrades = findViewById(R.id.nav_upgrades);
        navProjects = findViewById(R.id.nav_projects);
        navBoosts = findViewById(R.id.nav_boosts);
        navCasino = findViewById(R.id.nav_casino);

        navClick.setOnClickListener(v -> switchTab(TAB_CLICK));
        navUpgrades.setOnClickListener(v -> switchTab(TAB_UPGRADES));
        navProjects.setOnClickListener(v -> switchTab(TAB_PROJECTS));
        navBoosts.setOnClickListener(v -> switchTab(TAB_BOOSTS));
        navCasino.setOnClickListener(v -> switchTab(TAB_CASINO));

        // Dev button
        findViewById(R.id.btn_dev).setOnClickListener(v -> showDevMenu());

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

    private void showDevMenu() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        Button btnSpawnByte = new Button(this);
        btnSpawnByte.setText("🔴 Spawn Byte");
        btnSpawnByte.setTextColor(0xFFFFFFFF);
        btnSpawnByte.setBackgroundTintList(ColorStateList.valueOf(0xFFE63946));
        layout.addView(btnSpawnByte);

        Button btnAddCoins = new Button(this);
        btnAddCoins.setText("💰 +10K Coins");
        btnAddCoins.setTextColor(0xFFFFFFFF);
        btnAddCoins.setBackgroundTintList(ColorStateList.valueOf(0xFFF7931A));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 16;
        btnAddCoins.setLayoutParams(lp);
        layout.addView(btnAddCoins);

        AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog)
            .setTitle("🛠 Dev Tools")
            .setView(layout)
            .setNegativeButton("Close", null)
            .create();

        btnSpawnByte.setOnClickListener(v -> {
            byteFloater.forceSpawn();
            dialog.dismiss();
        });
        btnAddCoins.setOnClickListener(v -> {
            bridge.nativeAddCoins(10000);
            updateUI();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void onByteCaught() {
        bytesCollected++;
        if (bytesCollected >= BYTES_NEEDED) {
            bytesCollected = 0;
            activateByteBonus();
            Toast toast = Toast.makeText(this,
                "\uD83D\uDD34 3/3 BYTES COLLECTED! 5x INCOME FOR 60s! \uD83D\uDD34",
                Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this,
                "\uD83D\uDD34 " + bytesCollected + "/" + BYTES_NEEDED + " bytes collected",
                Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private void activateByteBonus() {
        // Cancel any existing expiry
        if (byteBonusExpiry != null) {
            handler.removeCallbacks(byteBonusExpiry);
        }
        byteBonusActive = true;
        bridge.nativeSetByteBonus(BYTE_BONUS_MULTIPLIER);
        tvByteBonus.setVisibility(View.VISIBLE);

        byteBonusExpiry = () -> {
            byteBonusActive = false;
            bridge.nativeClearByteBonus();
            tvByteBonus.setVisibility(View.GONE);
        };
        handler.postDelayed(byteBonusExpiry, BYTE_BONUS_DURATION_MS);
    }

    private void switchTab(int tab) {
        currentTab = tab;
        tabClick.setVisibility(tab == TAB_CLICK ? View.VISIBLE : View.GONE);
        tabUpgrades.setVisibility(tab == TAB_UPGRADES ? View.VISIBLE : View.GONE);
        tabProjects.setVisibility(tab == TAB_PROJECTS ? View.VISIBLE : View.GONE);
        tabBoosts.setVisibility(tab == TAB_BOOSTS ? View.VISIBLE : View.GONE);
        tabCasino.setVisibility(tab == TAB_CASINO ? View.VISIBLE : View.GONE);

        setNavStyle(navClick, tab == TAB_CLICK);
        setNavStyle(navUpgrades, tab == TAB_UPGRADES);
        setNavStyle(navProjects, tab == TAB_PROJECTS);
        setNavStyle(navBoosts, tab == TAB_BOOSTS);
        setNavStyle(navCasino, tab == TAB_CASINO);

        if (tab == TAB_UPGRADES) {
            upgradeAdapter.notifyDataSetChanged();
        } else if (tab == TAB_PROJECTS) {
            refreshProjectsTab();
        } else if (tab == TAB_BOOSTS) {
            boostAdapter.notifyDataSetChanged();
        } else if (tab == TAB_CASINO) {
            refreshCasinoTab();
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

        if (currentTab == TAB_CASINO) {
            updateCasinoBalance();
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

    private void startProjectDirectly(int projectIndex) {
        int roleCount = bridge.nativeGetProjectRequiredRoleCount(projectIndex);
        int[] assignedRoles = new int[roleCount];
        int[] assignedCounts = new int[roleCount];

        for (int i = 0; i < roleCount; i++) {
            String roleName = bridge.nativeGetProjectRequiredRoleName(projectIndex, i);
            int required = bridge.nativeGetProjectRequiredRoleAmount(projectIndex, i);
            assignedRoles[i] = roleNameToOrdinal(roleName);
            assignedCounts[i] = required;
        }

        // Check if player has enough workers
        for (int i = 0; i < roleCount; i++) {
            int avail = bridge.nativeGetWorkerAvailable(assignedRoles[i]);
            if (avail < assignedCounts[i]) {
                String roleName = bridge.nativeGetProjectRequiredRoleName(projectIndex, i);
                Toast.makeText(this, "Not enough " + roleName + "s available", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        bridge.nativeStartProject(projectIndex, assignedRoles, assignedCounts, roleCount);
        refreshProjectsTab();
        updateUI();
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

    // ===== Mines Casino Methods =====

    private void refreshCasinoTab() {
        updateCasinoBalance();
        int minesState = bridge.nativeMinesGetState();
        if (minesState == 1) {
            // Game active - show game section
            minesSetup.setVisibility(View.GONE);
            minesGameSection.setVisibility(View.VISIBLE);
            if (mineTiles == null) buildMinesGrid();
            updateMineTiles();
            updateMinesDisplay();
            updateMinesActionButton();
        } else if (minesState == 2 || minesState == 3) {
            // Game finished - show result
            minesSetup.setVisibility(View.GONE);
            minesGameSection.setVisibility(View.VISIBLE);
            if (mineTiles != null) updateMineTiles();
            updateMinesDisplay();
            btnMinesAction.setText("\uD83C\uDFAE NEW GAME");
            btnMinesAction.setEnabled(true);
            btnMinesAction.setAlpha(1.0f);
            btnMinesAction.setBackgroundTintList(ColorStateList.valueOf(0xFFF7931A));
        } else {
            // No game - show setup
            minesSetup.setVisibility(View.VISIBLE);
            minesGameSection.setVisibility(View.GONE);
        }
    }

    private void updateCasinoBalance() {
        double maxBet = bridge.nativeMinesGetMaxBet();
        tvCasinoBalance.setText("Max Bet: " + formatNumber(maxBet) + " BTC (10% of balance)");
    }

    private void startMinesGame() {
        String betText = etMinesBet.getText().toString().trim();
        if (betText.isEmpty()) {
            Toast.makeText(this, "Enter a bet amount", Toast.LENGTH_SHORT).show();
            return;
        }
        double bet;
        try {
            bet = Double.parseDouble(betText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid bet amount", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bet <= 0) {
            Toast.makeText(this, "Bet must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }
        double maxBet = bridge.nativeMinesGetMaxBet();
        if (bet > maxBet) {
            Toast.makeText(this, "Max bet is " + formatNumber(maxBet) + " BTC (10%)", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean started = bridge.nativeMinesStart(selectedMineCount, bet);
        if (!started) {
            Toast.makeText(this, "Could not start game. Check balance.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide keyboard
        android.view.inputmethod.InputMethodManager imm =
            (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etMinesBet.getWindowToken(), 0);

        minesSetup.setVisibility(View.GONE);
        minesGameSection.setVisibility(View.VISIBLE);
        tvMinesResult.setVisibility(View.GONE);

        buildMinesGrid();
        updateMinesDisplay();
        updateMinesActionButton();
        updateUI();
    }

    private void buildMinesGrid() {
        minesGridContainer.removeAllViews();
        mineTiles = new TextView[25];
        int marginPx = dpToPx(3);

        for (int row = 0; row < 5; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER);

            for (int col = 0; col < 5; col++) {
                int index = row * 5 + col;
                TextView tile = new TextView(this);
                tile.setText("?");
                tile.setGravity(Gravity.CENTER);
                tile.setTextSize(22);
                tile.setTextColor(0xFFFFFFFF);
                tile.setBackgroundColor(0xFF0F3460);
                tile.setTypeface(null, android.graphics.Typeface.BOLD);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, dpToPx(56), 1.0f);
                params.setMargins(marginPx, marginPx, marginPx, marginPx);
                tile.setLayoutParams(params);

                final int idx = index;
                tile.setOnClickListener(v -> onMineTileClick(idx));

                rowLayout.addView(tile);
                mineTiles[index] = tile;
            }

            minesGridContainer.addView(rowLayout);
        }
    }

    private void onMineTileClick(int index) {
        int state = bridge.nativeMinesGetState();
        if (state != 1) return;

        int result = bridge.nativeMinesReveal(index);
        if (result < 0) return;

        // Animate the clicked tile
        if (mineTiles != null && index >= 0 && index < 25) {
            ScaleAnimation anim = new ScaleAnimation(
                1.0f, 0.85f, 1.0f, 0.85f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(60);
            anim.setRepeatCount(1);
            anim.setRepeatMode(ScaleAnimation.REVERSE);
            mineTiles[index].startAnimation(anim);
        }

        updateMineTiles();

        if (result == 2) {
            // Mine hit - loss
            showMinesResult(false, 0);
        } else {
            int newState = bridge.nativeMinesGetState();
            if (newState == 2) {
                // All safe tiles found - auto cash out
                double winnings = bridge.nativeMinesCashOut();
                showMinesResult(true, winnings);
            } else {
                updateMinesDisplay();
                updateMinesActionButton();
            }
        }
        updateUI();
    }

    private void cashOutMines() {
        double winnings = bridge.nativeMinesCashOut();
        updateMineTiles();
        showMinesResult(true, winnings);
        updateUI();
    }

    private void showMinesResult(boolean won, double amount) {
        tvMinesResult.setVisibility(View.VISIBLE);
        if (won) {
            double bet = bridge.nativeMinesGetBet();
            double profit = amount - bet;
            tvMinesResult.setText("\uD83D\uDCB0 WON " + formatNumber(amount) + " BTC (+" + formatNumber(profit) + " profit)");
            tvMinesResult.setTextColor(0xFF4EC9B0);
        } else {
            tvMinesResult.setText("\uD83D\uDCA3 BOOM! Lost " + formatNumber(bridge.nativeMinesGetBet()) + " BTC");
            tvMinesResult.setTextColor(0xFFE74C3C);
        }

        btnMinesAction.setText("\uD83C\uDFAE NEW GAME");
        btnMinesAction.setEnabled(true);
        btnMinesAction.setAlpha(1.0f);
        btnMinesAction.setBackgroundTintList(ColorStateList.valueOf(0xFFF7931A));

        updateMinesDisplay();

        // Disable tile clicks
        if (mineTiles != null) {
            for (TextView tile : mineTiles) {
                tile.setClickable(false);
            }
        }
    }

    private void resetMinesUI() {
        minesSetup.setVisibility(View.VISIBLE);
        minesGameSection.setVisibility(View.GONE);
        tvMinesResult.setVisibility(View.GONE);
        updateCasinoBalance();
    }

    private void updateMinesDisplay() {
        double mult = bridge.nativeMinesGetMultiplier();
        double nextMult = bridge.nativeMinesGetNextMultiplier();
        double potential = bridge.nativeMinesGetPotentialWin();

        tvMinesMultiplier.setText(String.format("%.2fx", mult));
        if (nextMult > 0) {
            tvMinesNext.setText(String.format("  \u2192 %.2fx", nextMult));
            tvMinesNext.setVisibility(View.VISIBLE);
        } else {
            tvMinesNext.setVisibility(View.GONE);
        }
        tvMinesPotential.setText("Potential: " + formatNumber(potential) + " BTC");

        int revealed = bridge.nativeMinesGetTilesRevealed();
        int mines = bridge.nativeMinesGetMineCount();
        tvMinesInfo.setText("Bet: " + formatNumber(bridge.nativeMinesGetBet())
            + " BTC  |  " + mines + " mines  |  " + revealed + " revealed");
    }

    private void updateMinesActionButton() {
        int state = bridge.nativeMinesGetState();
        if (state == 1) {
            int revealed = bridge.nativeMinesGetTilesRevealed();
            double potential = bridge.nativeMinesGetPotentialWin();
            if (revealed == 0) {
                btnMinesAction.setText("\uD83D\uDCB0 CASH OUT");
                btnMinesAction.setEnabled(false);
                btnMinesAction.setAlpha(0.5f);
            } else {
                btnMinesAction.setText("\uD83D\uDCB0 CASH OUT (" + formatNumber(potential) + " BTC)");
                btnMinesAction.setEnabled(true);
                btnMinesAction.setAlpha(1.0f);
            }
            btnMinesAction.setBackgroundTintList(ColorStateList.valueOf(0xFF4EC9B0));
        } else {
            btnMinesAction.setText("\uD83C\uDFAE NEW GAME");
            btnMinesAction.setEnabled(true);
            btnMinesAction.setAlpha(1.0f);
            btnMinesAction.setBackgroundTintList(ColorStateList.valueOf(0xFFF7931A));
        }
    }

    private void updateMineTiles() {
        if (mineTiles == null) return;
        for (int i = 0; i < 25; i++) {
            int tileState = bridge.nativeMinesGetTileState(i);
            switch (tileState) {
                case 0: // hidden
                    mineTiles[i].setText("?");
                    mineTiles[i].setBackgroundColor(0xFF0F3460);
                    mineTiles[i].setTextColor(0xFFFFFFFF);
                    mineTiles[i].setClickable(true);
                    break;
                case 1: // safe
                    mineTiles[i].setText("\uD83D\uDC8E");
                    mineTiles[i].setBackgroundColor(0xFF2ECC71);
                    mineTiles[i].setTextColor(0xFFFFFFFF);
                    mineTiles[i].setClickable(false);
                    break;
                case 2: // mine
                    mineTiles[i].setText("\uD83D\uDCA3");
                    mineTiles[i].setBackgroundColor(0xFFE74C3C);
                    mineTiles[i].setTextColor(0xFFFFFFFF);
                    mineTiles[i].setClickable(false);
                    break;
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
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
            boolean locked = bridge.nativeIsUpgradeLocked(position);

            // Hide locked upgrades entirely
            if (locked) {
                holder.itemView.setVisibility(View.GONE);
                return;
            }
            
            holder.itemView.setVisibility(View.VISIBLE);
            holder.tvName.setText(name + " (" + owned + ")");
            holder.tvDesc.setText(desc);
            holder.tvDesc.setTextColor(0xFFCCCCCC);
            holder.tvCost.setText("Cost: " + formatNumber(cost) + " BTC");
            holder.tvIncome.setText("Earning: " + formatNumber(income) + " BTC/sec");
            
            // Show indicator if next upgrade exists and is locked
            int nextPos = position + 1;
            if (nextPos < bridge.nativeGetUpgradeCount() && bridge.nativeIsUpgradeLocked(nextPos)) {
                holder.tvNextLocked.setVisibility(View.VISIBLE);
            } else {
                holder.tvNextLocked.setVisibility(View.GONE);
            }
            
            boolean canBuy = coins >= cost;
            holder.btnBuy.setEnabled(canBuy);
            holder.btnBuy.setAlpha(canBuy ? 1.0f : 0.5f);
        }

        @Override
        public int getItemCount() {
            return bridge.nativeGetUpgradeCount();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvDesc, tvCost, tvIncome, tvNextLocked;
            Button btnBuy;

            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_upgrade_name);
                tvDesc = v.findViewById(R.id.tv_upgrade_desc);
                tvCost = v.findViewById(R.id.tv_upgrade_cost);
                tvIncome = v.findViewById(R.id.tv_upgrade_income);
                tvNextLocked = v.findViewById(R.id.tv_next_locked);
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
                    startProjectDirectly(position);
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
            boolean locked = bridge.nativeIsProjectLocked(position);

            // Hide locked projects entirely
            if (locked) {
                holder.itemView.setVisibility(View.GONE);
                return;
            }

            holder.itemView.setVisibility(View.VISIBLE);
            holder.tvName.setText(name);
            holder.tvName.setTextColor(0xFFFFFFFF);
            holder.tvDesc.setText(desc);
            holder.tvDesc.setTextColor(0xFFCCCCCC);
            holder.tvTime.setTextColor(0xFFCCCCCC);
            holder.tvReward.setTextColor(0xFFCCCCCC);
            holder.tvDifficulty.setTextColor(0xFFCCCCCC);
            holder.tvRoles.setTextColor(0xFFCCCCCC);
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

            // Show indicator if next project exists and is locked
            int nextPos = position + 1;
            if (nextPos < bridge.nativeGetProjectCount() && bridge.nativeIsProjectLocked(nextPos)) {
                holder.tvNextLockedProject.setVisibility(View.VISIBLE);
            } else {
                holder.tvNextLockedProject.setVisibility(View.GONE);
            }

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
            TextView tvName, tvDesc, tvTime, tvReward, tvDifficulty, tvRoles, tvNextLockedProject;
            Button btnStart;

            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_project_name);
                tvDesc = v.findViewById(R.id.tv_project_desc);
                tvTime = v.findViewById(R.id.tv_project_time);
                tvReward = v.findViewById(R.id.tv_project_reward);
                tvDifficulty = v.findViewById(R.id.tv_project_difficulty);
                tvRoles = v.findViewById(R.id.tv_project_roles);
                tvNextLockedProject = v.findViewById(R.id.tv_next_locked_project);
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
