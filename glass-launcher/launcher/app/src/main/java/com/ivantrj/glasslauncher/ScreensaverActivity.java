package com.ivantrj.glasslauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The koi pond screensaver: nagomi (screensaver/nagomi, built into assets/nagomi) in a WebView.
 * Every start gets a new random pond; its sky follows the real time of day and weather
 * (PondWeather), and 1 in 20 ponds has a giant koi. Any key closes it.
 *
 * Started by HomeButtonService after the idle time set in Glass's Settings menu, by
 * "Preview now", or on demand from the Koi Pond tile (Glass's grid) or entry (Fire TV's Your Apps). Debug extras for screenshots: --es weather moonlight --ez rain true --ez giant true.
 *
 * Runs in its own process (":pond", see the manifest) so the WebView never loads into the process
 * of HomeButtonService. Nothing here may rely on static state shared with the rest of Glass.
 */
public class ScreensaverActivity extends Activity {

    private static final String TAG = "Screensaver";
    private static final long WEATHER_REFRESH = 15 * 60_000L;
    private static final int GIANT_ODDS = 20;

    static void start(Context context) {
        context.startActivity(new Intent(context, ScreensaverActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private WebView web;
    private int seed;
    private boolean pinnedLook; // weather forced by a debug extra: no live updates
    private String shownLook; // what the pond shows now, as PondWeather.Look.toString()
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        // Keep the screen on only as long as the stick would normally stay awake, then let it sleep
        long sleepAfter = Settings.System.getLong(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15 * 60_000L);
        main.postDelayed(() -> getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON), sleepAfter);

        Intent in = getIntent();
        Random random = new Random();
        seed = random.nextInt(Integer.MAX_VALUE);
        boolean giant = in.hasExtra("giant") ? in.getBooleanExtra("giant", false) : random.nextInt(GIANT_ODDS) == 0;
        PondWeather.Look look = PondWeather.current(this, seed);
        if (in.hasExtra("weather")) {
            look = new PondWeather.Look(in.getStringExtra("weather"), in.getBooleanExtra("rain", false));
            pinnedLook = true;
        }
        shownLook = look.toString();
        Log.i(TAG, "seed " + seed + ", " + look + (giant ? ", giant koi" : ""));

        WebViewAssetLoader assets = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();
        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        web.setAlpha(0f);
        web.setFocusable(false); // keys go to this activity, which closes on any of them
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assets.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // A moment for the first frames to draw, then fade the pond in from black
                view.animate().alpha(1f).setStartDelay(400).setDuration(1200).start();
            }
        });
        setContentView(web);
        web.loadUrl(new Uri.Builder().scheme("https").authority(WebViewAssetLoader.DEFAULT_DOMAIN)
                .path("/assets/nagomi/index.html")
                .appendQueryParameter("tv", "1")
                .appendQueryParameter("seed", String.valueOf(seed))
                .appendQueryParameter("weather", look.preset)
                .appendQueryParameter("rain", look.rain ? "1" : "0")
                .appendQueryParameter("giant", giant ? "1" : "0")
                .build().toString());

        if (!pinnedLook) refreshWeather.run();
    }

    /** Fetches fresh weather off the main thread and pushes the look to the pond if it changed. */
    private final Runnable refreshWeather = new Runnable() {
        @Override public void run() {
            io.execute(() -> {
                PondWeather.refresh(getApplicationContext());
                PondWeather.Look look = PondWeather.current(getApplicationContext(), seed);
                main.post(() -> {
                    if (web == null || look.toString().equals(shownLook)) return;
                    shownLook = look.toString();
                    Log.i(TAG, "weather now " + look);
                    web.evaluateJavascript("window.glassTv && glassTv.setWeather('" + look.preset + "', "
                            + look.rain + ")", null);
                });
            });
            main.postDelayed(this, WEATHER_REFRESH);
        }
    };

    /** Key whose press started here; -1 until one does. */
    private int pressedKey = -1;

    /**
     * Any key closes it. Down is swallowed and the close happens on up, so the app behind never sees half a press.
     * Only an up whose down came here counts: opened from a tile, the tail of that Select press must not close it.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) pressedKey = event.getKeyCode();
        else if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == pressedKey) finish();
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish(); // Home, another app or sleep: a screensaver never waits in the background
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        main.removeCallbacksAndMessages(null);
        io.shutdownNow();
        if (web != null) {
            web.destroy();
            web = null;
        }
    }
}
