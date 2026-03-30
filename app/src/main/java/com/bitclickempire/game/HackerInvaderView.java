package com.bitclickempire.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Overlay view that spawns "hacker" invaders from the edges of the screen.
 * They slowly inch toward the center bitcoin. Tapping them kills them for a coin reward.
 * If they reach the bitcoin, they steal a small amount of coins.
 */
public class HackerInvaderView extends View {

    public interface OnHackerEventListener {
        /** Called when the user taps and kills a hacker. */
        void onHackerKilled(float x, float y);
        /** Called when a hacker reaches the bitcoin and steals coins. */
        void onHackerReachedBitcoin();
    }

    private OnHackerEventListener listener;

    // Spawn timing
    private static final long SPAWN_CHECK_MS = 4000;       // check every 4 seconds
    private static final double SPAWN_CHANCE = 0.25;        // 25% chance per check
    private static final int MAX_HACKERS = 5;               // max on screen at once

    // Hacker appearance
    private static final float HACKER_SIZE = 48f;
    private static final float TAP_RADIUS = 80f;            // hit-area radius in px
    private static final float ARRIVAL_RADIUS = 90f;        // how close to center = "reached"

    // Movement
    private static final float SPEED_MIN = 0.4f;
    private static final float SPEED_MAX = 1.0f;

    // Visual wobble
    private static final float WOBBLE_AMPLITUDE = 1.5f;

    private static final String[] HACKER_EMOJIS = {"👾", "🤖", "💀", "🐛", "☠️"};
    private static final int[] HACKER_COLORS = {
        0xFFFF0040, // red
        0xFF00FF88, // neon green (matrix)
        0xFF9D00FF, // purple
        0xFFFF6600, // orange
        0xFF00DDFF, // cyan
    };

    private final List<Hacker> hackers = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint warningPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    private long lastSpawnCheck;
    private float centerX, centerY;

    public HackerInvaderView(Context context) {
        super(context);
        init();
    }

    public HackerInvaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HackerInvaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(HACKER_SIZE);

        glowPaint.setTextAlign(Paint.Align.CENTER);
        glowPaint.setFakeBoldText(true);

        warningPaint.setTextAlign(Paint.Align.CENTER);
        warningPaint.setFakeBoldText(true);
        warningPaint.setTextSize(14f);
        warningPaint.setColor(0xFFFF0040);

        lastSpawnCheck = System.currentTimeMillis();
        // Redraw loop
        postInvalidateOnAnimation();
    }

    public void setOnHackerEventListener(OnHackerEventListener l) {
        this.listener = l;
    }

    /** Force-spawn a hacker for dev testing. */
    public void forceSpawn() {
        spawnHacker();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
    }

    private void spawnHacker() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;
        if (hackers.size() >= MAX_HACKERS) return;

        Hacker hk = new Hacker();
        hk.birthTime = System.currentTimeMillis();
        hk.speed = SPEED_MIN + random.nextFloat() * (SPEED_MAX - SPEED_MIN);
        hk.wobblePhase = random.nextFloat() * (float) (2 * Math.PI);
        hk.wobbleSpeed = 0.05f + random.nextFloat() * 0.05f;
        hk.emoji = HACKER_EMOJIS[random.nextInt(HACKER_EMOJIS.length)];
        hk.color = HACKER_COLORS[random.nextInt(HACKER_COLORS.length)];

        // Spawn from a random edge
        int edge = random.nextInt(4);
        float margin = HACKER_SIZE;
        switch (edge) {
            case 0: // left
                hk.x = -margin;
                hk.y = h * 0.15f + random.nextFloat() * h * 0.7f;
                break;
            case 1: // right
                hk.x = w + margin;
                hk.y = h * 0.15f + random.nextFloat() * h * 0.7f;
                break;
            case 2: // top
                hk.x = w * 0.15f + random.nextFloat() * w * 0.7f;
                hk.y = -margin;
                break;
            default: // bottom
                hk.x = w * 0.15f + random.nextFloat() * w * 0.7f;
                hk.y = h + margin;
                break;
        }

        hackers.add(hk);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float tx = event.getX();
            float ty = event.getY();

            // Check from newest to oldest (top-drawn first)
            for (int i = hackers.size() - 1; i >= 0; i--) {
                Hacker hk = hackers.get(i);
                float dx = tx - hk.x;
                float dy = ty - hk.y;
                if (dx * dx + dy * dy <= TAP_RADIUS * TAP_RADIUS) {
                    // Killed!
                    float killX = hk.x;
                    float killY = hk.y;
                    hackers.remove(i);
                    if (listener != null) listener.onHackerKilled(killX, killY);
                    invalidate();
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now = System.currentTimeMillis();

        // Periodic spawn check
        if (now - lastSpawnCheck >= SPAWN_CHECK_MS) {
            lastSpawnCheck = now;
            if (random.nextDouble() < SPAWN_CHANCE) {
                spawnHacker();
            }
        }

        // Update and draw hackers
        Iterator<Hacker> it = hackers.iterator();
        while (it.hasNext()) {
            Hacker hk = it.next();

            // Move toward center
            float dx = centerX - hk.x;
            float dy = centerY - hk.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < ARRIVAL_RADIUS) {
                // Reached the bitcoin!
                it.remove();
                if (listener != null) listener.onHackerReachedBitcoin();
                continue;
            }

            // Normalize direction and move
            float nx = dx / dist;
            float ny = dy / dist;
            hk.x += nx * hk.speed;
            hk.y += ny * hk.speed;

            // Add wobble perpendicular to movement direction
            hk.wobblePhase += hk.wobbleSpeed;
            float wobble = (float) Math.sin(hk.wobblePhase) * WOBBLE_AMPLITUDE;
            hk.x += -ny * wobble;  // perpendicular
            hk.y += nx * wobble;

            // Pulsing effect
            long age = now - hk.birthTime;
            float pulse = 1.0f + 0.15f * (float) Math.sin(age * 0.004);

            // Proximity warning — the closer to center, the more urgent
            float proximity = 1.0f - Math.min(dist / (Math.max(getWidth(), getHeight()) * 0.5f), 1.0f);

            // Glow effect (gets brighter as it gets closer)
            int glowAlpha = (int) (30 + proximity * 60);
            glowPaint.setColor(hk.color);
            glowPaint.setAlpha(glowAlpha);
            glowPaint.setTextSize(HACKER_SIZE * pulse * 1.4f);
            canvas.drawText(hk.emoji, hk.x, hk.y, glowPaint);

            // Main emoji
            paint.setTextSize(HACKER_SIZE * pulse);
            paint.setColor(0xFFFFFFFF);
            paint.setAlpha(255);
            canvas.drawText(hk.emoji, hk.x, hk.y, paint);

            // Warning text when close
            if (proximity > 0.6f) {
                float warningAlpha = (proximity - 0.6f) / 0.4f;
                warningPaint.setAlpha((int) (255 * warningAlpha * (0.5f + 0.5f * (float) Math.sin(age * 0.01))));
                warningPaint.setTextSize(12f + proximity * 4f);
                canvas.drawText("⚠ HACK", hk.x, hk.y - HACKER_SIZE * 0.7f, warningPaint);
            }
        }

        // Always keep animating
        postInvalidateOnAnimation();
    }

    private static class Hacker {
        float x, y;
        float speed;
        float wobblePhase;
        float wobbleSpeed;
        String emoji;
        int color;
        long birthTime;
    }
}
