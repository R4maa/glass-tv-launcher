package com.ivantrj.glasslauncher;

import android.accessibilityservice.AccessibilityService;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Fire OS protects its launcher, so it can't be disabled and stays the system
 * home. This service makes Glass the effective home screen instead:
 *  - the remote's Home button is consumed and opens Glass;
 *  - whenever the Amazon launcher comes to the front anyway (after boot, or when
 *    an app exits), Glass is brought straight back on top.
 *  - every key press re-arms the screensaver's idle timer (any app, not just Glass).
 * Event-driven only: it receives window changes and key events, and does
 * nothing in between.
 */
public class HomeButtonService extends AccessibilityService {

    static final String AMAZON_LAUNCHER = "com.amazon.tv.launcher";
    private static final long RECHECK = 60_000L;
    private static HomeButtonService running;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastLaunch;

    /**
     * Set by the "Fire TV Home" tile: Amazon's home screen is left alone until the next Home
     * press or until another app's screen opens, so it can be browsed. The stick's remote doesn't
     * always reach the key filter, and without the second way out the visit never ended.
     * In memory only, so a restart always brings Glass back.
     */
    private static volatile boolean visitingFireHome;

    /** Opens Amazon's home screen without the redirect bouncing it back. False if there is none. */
    static boolean visitFireHome(Context context) {
        Intent amazonHome = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .setPackage(AMAZON_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (amazonHome.resolveActivity(context.getPackageManager()) == null) return false;
        visitingFireHome = true;
        try {
            context.startActivity(amazonHome);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            visitingFireHome = false;
            return false;
        }
    }

    /** Idle minutes before the koi screensaver starts; 0 = off. Set in Glass's Settings menu. */
    static int screensaverMinutes(Context context) {
        return context.getSharedPreferences("home", MODE_PRIVATE).getInt("screensaverMinutes", 10);
    }

    static void setScreensaverMinutes(Context context, int minutes) {
        context.getSharedPreferences("home", MODE_PRIVATE).edit().putInt("screensaverMinutes", minutes).apply();
        if (running != null) running.armScreensaver();
    }

    @Override
    protected void onServiceConnected() {
        running = this;
        armScreensaver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = null;
        handler.removeCallbacksAndMessages(null);
    }

    private void armScreensaver() {
        handler.removeCallbacks(idle);
        int minutes = screensaverMinutes(this);
        if (minutes > 0) handler.postDelayed(idle, minutes * 60_000L);
    }

    /** The idle time has passed: show the pond, unless something is playing or the screen is off. */
    private final Runnable idle = () -> {
        if (ScreensaverActivity.showing) return;
        if (busy()) {
            handler.postDelayed(this.idle, RECHECK);
            return;
        }
        ScreensaverActivity.start(this);
    };

    private boolean busy() {
        if (!getSystemService(PowerManager.class).isInteractive()) return true;
        if (getSystemService(AudioManager.class).isMusicActive()) return true; // films and music both count
        try {
            for (MediaController c : getSystemService(MediaSessionManager.class)
                    .getActiveSessions(new ComponentName(this, MediaListener.class))) {
                PlaybackState state = c.getPlaybackState();
                // Fire TV's YouTube leaves a session at "playing" with speed 0 after it stops: not playing
                if (state != null && state.getState() == PlaybackState.STATE_PLAYING
                        && state.getPlaybackSpeed() != 0f) return true;
            }
        } catch (SecurityException ignored) {
            // no notification access: the audio check above has to do
        }
        return false;
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) armScreensaver();
        if (event.getKeyCode() != KeyEvent.KEYCODE_HOME) return false;
        if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
            visitingFireHome = false; // Home always ends a visit to Amazon's home
            openGlass();
        }
        return true; // swallow both down and up so Fire OS never sees Home
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Only Amazon's home screen itself: Fire OS Settings (MainSettingsActivity) and the side
        // menu live in the same package, and reacting to those would bounce them back to Glass.
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence pkg = event.getPackageName();
        CharSequence cls = event.getClassName();
        if (pkg == null || cls == null) return;
        if (!AMAZON_LAUNCHER.contentEquals(pkg)) {
            if (visitingFireHome && isOtherAppsScreen(pkg.toString(), cls.toString())) visitingFireHome = false;
            return;
        }
        // Fire OS Settings lives in the launcher's package: opening it also ends a visit (its
        // details pages don't, so Back from one stays on Amazon's home)
        if (cls.toString().contains("SettingsActivity")) visitingFireHome = false;
        else if (!visitingFireHome && cls.toString().contains("HomeActivity")) openGlass();
    }

    /** A real activity of another app, not Glass, a system overlay or a popup window. */
    private boolean isOtherAppsScreen(String pkg, String cls) {
        if (pkg.equals(getPackageName()) || pkg.equals("com.android.systemui")) return false;
        try {
            getPackageManager().getActivityInfo(new ComponentName(pkg, cls), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void openGlass() {
        long now = SystemClock.uptimeMillis();
        if (now - lastLaunch < 600) return; // one launch per Home press / launcher flash
        lastLaunch = now;
        startActivity(new Intent(this, HomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    @Override public void onInterrupt() {}
}
