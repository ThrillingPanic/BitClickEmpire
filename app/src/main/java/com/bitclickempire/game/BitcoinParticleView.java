package com.bitclickempire.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Overlay view that spawns small ₿ particles flying outward and fading on each click.
 */
public class BitcoinParticleView extends View {

    private static final int PARTICLES_PER_CLICK = 7;
    private static final long PARTICLE_LIFETIME_MS = 900;
    private static final float MIN_SPEED = 2.5f;
    private static final float MAX_SPEED = 7.0f;
    private static final float GRAVITY = 0.08f;
    private static final float MIN_SIZE = 14f;
    private static final float MAX_SIZE = 26f;

    private final List<Particle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private boolean running = false;

    private static final int[] COLORS = {
        0xFFF7931A, // bitcoin orange
        0xFFFFB347, // light orange
        0xFFFFD700, // gold
        0xFFFFA500, // orange
        0xFFE8820C, // dark orange
    };

    public BitcoinParticleView(Context context) {
        super(context);
        init();
    }

    public BitcoinParticleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BitcoinParticleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
    }

    /**
     * Spawn particles from a point (in this view's coordinate space).
     */
    public void spawnParticles(float x, float y) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < PARTICLES_PER_CLICK; i++) {
            Particle p = new Particle();
            p.x = x;
            p.y = y;
            // Random angle biased upward (full circle, but more upward)
            double angle = Math.toRadians(-90 + random.nextGaussian() * 60);
            float speed = MIN_SPEED + random.nextFloat() * (MAX_SPEED - MIN_SPEED);
            p.vx = (float) (Math.cos(angle) * speed);
            p.vy = (float) (Math.sin(angle) * speed);
            p.size = MIN_SIZE + random.nextFloat() * (MAX_SIZE - MIN_SIZE);
            p.color = COLORS[random.nextInt(COLORS.length)];
            p.birthTime = now;
            p.rotation = random.nextFloat() * 40 - 20; // slight random rotation
            p.rotationSpeed = random.nextFloat() * 4 - 2;
            particles.add(p);
        }
        if (!running) {
            running = true;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (particles.isEmpty()) {
            running = false;
            return;
        }

        long now = System.currentTimeMillis();
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            long age = now - p.birthTime;
            if (age > PARTICLE_LIFETIME_MS) {
                it.remove();
                continue;
            }

            float progress = (float) age / PARTICLE_LIFETIME_MS; // 0 → 1

            // Update position
            p.x += p.vx;
            p.vy += GRAVITY; // gravity pulls down slightly
            p.y += p.vy;
            p.rotation += p.rotationSpeed;

            // Fade out: fully opaque for first 30%, then fade
            int alpha;
            if (progress < 0.3f) {
                alpha = 255;
            } else {
                alpha = (int) (255 * (1.0f - (progress - 0.3f) / 0.7f));
            }
            alpha = Math.max(0, Math.min(255, alpha));

            // Shrink slightly toward end
            float scale = 1.0f - 0.3f * progress;

            paint.setColor(p.color);
            paint.setAlpha(alpha);
            paint.setTextSize(p.size * scale);

            canvas.save();
            canvas.rotate(p.rotation, p.x, p.y);
            canvas.drawText("₿", p.x, p.y, paint);
            canvas.restore();
        }

        // Keep animating if particles remain
        if (!particles.isEmpty()) {
            invalidate();
        } else {
            running = false;
        }
    }

    private static class Particle {
        float x, y;
        float vx, vy;
        float size;
        int color;
        long birthTime;
        float rotation;
        float rotationSpeed;
    }
}
