package com.bitclickempire.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

/**
 * Overlay view that occasionally spawns a red floating "₿" (a "byte")
 * that drifts across the screen. Tapping it triggers a callback.
 */
public class ByteFloaterView extends View {

    public interface OnByteCaughtListener {
        void onByteCaught();
    }

    private OnByteCaughtListener listener;

    // Spawn chance checked every SPAWN_CHECK_MS
    private static final long SPAWN_CHECK_MS = 3000;   // check every 3 seconds
    private static final double SPAWN_CHANCE = 0.03;    // ~3% per check → roughly 1 per 100s on average

    private static final float BYTE_SIZE = 48f;
    private static final float SPEED_MIN = 1.2f;
    private static final float SPEED_MAX = 2.5f;
    private static final float TAP_RADIUS = 80f;        // hit-area radius in px
    private static final long BYTE_LIFETIME_MS = 8000;   // disappears after 8s if not caught

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    private boolean byteActive = false;
    private float byteX, byteY;
    private float byteVX, byteVY;
    private long byteBirthTime;
    private long lastSpawnCheck;
    private float pulsePhase = 0f;

    // Red/crimson colour
    private static final int BYTE_COLOR = 0xFFE63946;
    private static final int GLOW_COLOR = 0x44E63946;

    public ByteFloaterView(Context context) {
        super(context);
        init();
    }

    public ByteFloaterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ByteFloaterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(BYTE_SIZE);
        paint.setColor(BYTE_COLOR);

        glowPaint.setTextAlign(Paint.Align.CENTER);
        glowPaint.setFakeBoldText(true);
        glowPaint.setColor(GLOW_COLOR);

        lastSpawnCheck = System.currentTimeMillis();
    }

    public void setOnByteCaughtListener(OnByteCaughtListener l) {
        this.listener = l;
    }

    /** Force-spawn for testing; normally spawned automatically. */
    public void forceSpawn() {
        spawnByte();
    }

    private void spawnByte() {
        if (byteActive) return;
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        byteActive = true;
        byteBirthTime = System.currentTimeMillis();
        pulsePhase = 0f;

        // Pick a random edge to enter from (0=left, 1=right, 2=top, 3=bottom)
        int edge = random.nextInt(4);
        float speed = SPEED_MIN + random.nextFloat() * (SPEED_MAX - SPEED_MIN);
        switch (edge) {
            case 0: // left
                byteX = -BYTE_SIZE;
                byteY = h * 0.2f + random.nextFloat() * h * 0.6f;
                byteVX = speed;
                byteVY = (random.nextFloat() - 0.5f) * speed * 0.5f;
                break;
            case 1: // right
                byteX = w + BYTE_SIZE;
                byteY = h * 0.2f + random.nextFloat() * h * 0.6f;
                byteVX = -speed;
                byteVY = (random.nextFloat() - 0.5f) * speed * 0.5f;
                break;
            case 2: // top
                byteX = w * 0.2f + random.nextFloat() * w * 0.6f;
                byteY = -BYTE_SIZE;
                byteVX = (random.nextFloat() - 0.5f) * speed * 0.5f;
                byteVY = speed;
                break;
            default: // bottom
                byteX = w * 0.2f + random.nextFloat() * w * 0.6f;
                byteY = h + BYTE_SIZE;
                byteVX = (random.nextFloat() - 0.5f) * speed * 0.5f;
                byteVY = -speed;
                break;
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && byteActive) {
            float dx = event.getX() - byteX;
            float dy = event.getY() - byteY;
            if (dx * dx + dy * dy <= TAP_RADIUS * TAP_RADIUS) {
                byteActive = false;
                if (listener != null) listener.onByteCaught();
                invalidate();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now = System.currentTimeMillis();

        // Periodic spawn check
        if (!byteActive && now - lastSpawnCheck >= SPAWN_CHECK_MS) {
            lastSpawnCheck = now;
            if (random.nextDouble() < SPAWN_CHANCE) {
                spawnByte();
            }
        }

        if (byteActive) {
            long age = now - byteBirthTime;
            if (age > BYTE_LIFETIME_MS) {
                // Timed out — disappeared
                byteActive = false;
            } else {
                // Update position
                byteX += byteVX;
                byteY += byteVY;

                // Check if completely off-screen
                int w = getWidth();
                int h = getHeight();
                if (byteX < -BYTE_SIZE * 2 || byteX > w + BYTE_SIZE * 2 ||
                    byteY < -BYTE_SIZE * 2 || byteY > h + BYTE_SIZE * 2) {
                    byteActive = false;
                } else {
                    // Pulsing glow effect
                    pulsePhase += 0.15f;
                    float pulse = 1.0f + 0.25f * (float) Math.sin(pulsePhase);
                    float glowSize = BYTE_SIZE * pulse * 1.3f;

                    // Draw glow
                    glowPaint.setTextSize(glowSize);
                    canvas.drawText("₿", byteX, byteY, glowPaint);

                    // Draw main byte symbol
                    float mainSize = BYTE_SIZE * pulse;
                    paint.setTextSize(mainSize);

                    // Slight alpha fade in last 2 seconds
                    if (age > BYTE_LIFETIME_MS - 2000) {
                        float fade = (float)(BYTE_LIFETIME_MS - age) / 2000f;
                        paint.setAlpha((int)(255 * fade));
                        glowPaint.setAlpha((int)(68 * fade)); // 68 = 0x44
                    } else {
                        paint.setAlpha(255);
                        glowPaint.setAlpha(68);
                    }

                    canvas.drawText("₿", byteX, byteY, paint);
                }
            }
        }

        // Always keep redrawing so we can check for spawns
        postInvalidateDelayed(32); // ~30fps
    }
}
