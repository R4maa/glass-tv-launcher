package com.ivantrj.glasslauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Finds launchable apps and splits them into the dock and the grid. Runs off the main thread. */
final class AppLoader {

    /** Pseudo package of the "Fire TV Home" tile, which opens Amazon's home screen (see HomeButtonService). */
    static final String FIRE_HOME = "glass.firetv-home";
    /** Pseudo package of the "Koi Pond" tile, which starts the screensaver on demand. */
    static final String KOI_POND = "glass.koi-pond";

    /** Pseudo package of the "Cast" tile, which opens Fire TV's screen-mirroring receiver. */
    static final String CAST = "glass.cast";
    /** Fire TV's Miracast receiver: phones and laptops can only connect while its screen is open. */
    static final String CAST_SINK = "com.amazon.cast.sink";
    private static final String CAST_SINK_ACTIVITY = "com.amazon.cast.sink.DisplayMirroringSinkActivity";

    /** Tiles that are Glass's own shortcuts rather than installed apps (no App info page). */
    static boolean isShortcut(String pkg) {
        return FIRE_HOME.equals(pkg) || KOI_POND.equals(pkg) || CAST.equals(pkg);
    }

    /** Opens the installed AirPlay receiver app (for iPhones, see airplay_packages), or null. */
    static Intent airplayIntent(Context context) {
        PackageManager pm = context.getPackageManager();
        for (String pkg : context.getResources().getStringArray(R.array.airplay_packages)) {
            Intent launch = pm.getLeanbackLaunchIntentForPackage(pkg);
            if (launch == null) launch = pm.getLaunchIntentForPackage(pkg);
            if (launch != null) return launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return null;
    }

    /** A screen that is being cast to: nobody presses the remote, but it isn't idle. */
    static boolean isCastScreen(Context context, String pkg) {
        if (CAST_SINK.equals(pkg)) return true;
        return Arrays.asList(context.getResources().getStringArray(R.array.airplay_packages)).contains(pkg);
    }

    static final class Result {
        final List<AppInfo> dock = new ArrayList<>();
        final List<AppInfo> grid = new ArrayList<>();
    }

    static Result load(Context context) {
        PackageManager pm = context.getPackageManager();
        Map<String, ActivityInfo> activities = new LinkedHashMap<>();
        // TV (leanback) activities win over phone-style ones from the same package
        collect(pm, Intent.CATEGORY_LEANBACK_LAUNCHER, activities);
        collect(pm, Intent.CATEGORY_LAUNCHER, activities);
        activities.remove(context.getPackageName());
        for (String pkg : context.getResources().getStringArray(R.array.hidden_packages)) activities.remove(pkg);
        for (String pkg : readList(context, "hidden.txt")) activities.remove(pkg);

        Map<String, AppInfo> apps = new LinkedHashMap<>();
        for (ActivityInfo ai : activities.values()) {
            AppInfo app = toApp(pm, ai);
            if (app != null) apps.put(app.packageName, app);
        }
        apps.put(FIRE_HOME, fireHomeTile(context));
        apps.put(KOI_POND, koiPondTile(context));
        AppInfo cast = castTile(context);
        if (cast != null) apps.put(CAST, cast);

        Result result = new Result();
        List<String> dock = readList(context, "dock.txt");
        // An empty dock.txt is a dock the user emptied on screen; only a missing file means defaults
        if (dock.isEmpty() && !new File(context.getFilesDir(), "dock.txt").isFile()) {
            dock = Arrays.asList(context.getResources().getStringArray(R.array.dock_packages));
        }
        for (String pkg : dock) {
            AppInfo app = apps.remove(pkg);
            if (app != null && result.dock.size() < 4) result.dock.add(app);
        }
        result.grid.addAll(apps.values());
        Collator collator = Collator.getInstance();
        result.grid.sort((a, b) -> collator.compare(a.label, b.label));
        // order.txt (written when a tile is moved on screen) pins the grid order; apps not in it
        // (newly installed) keep their alphabetical place after the ordered ones.
        List<String> order = readList(context, "order.txt");
        if (!order.isEmpty()) {
            result.grid.sort((a, b) -> Integer.compare(rank(order, a), rank(order, b)));
        }
        return result;
    }

    private static int rank(List<String> order, AppInfo app) {
        int i = order.indexOf(app.packageName);
        return i < 0 ? Integer.MAX_VALUE : i;
    }

    /** Writes one package per line to a file in the app's private files dir (see readList). */
    static void writeList(Context context, String fileName, List<AppInfo> apps) {
        StringBuilder sb = new StringBuilder();
        for (AppInfo app : apps) sb.append(app.packageName).append('\n');
        try (FileWriter w = new FileWriter(new File(context.getFilesDir(), fileName))) {
            w.write(sb.toString());
        } catch (Exception ignored) {
        }
    }

    /**
     * One package per line from a file in the app's private files dir. configure.sh
     * writes these over ADB: dock.txt replaces the dock from res/values/config.xml,
     * hidden.txt adds to the built-in hidden list.
     */
    static List<String> readList(Context context, String fileName) {
        List<String> pkgs = new ArrayList<>();
        File file = new File(context.getFilesDir(), fileName);
        if (!file.isFile()) return pkgs;
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) pkgs.add(line);
            }
        } catch (Exception ignored) {
        }
        return pkgs;
    }

    private static void collect(PackageManager pm, String category, Map<String, ActivityInfo> out) {
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(category);
        for (ResolveInfo ri : pm.queryIntentActivities(intent, 0)) {
            if (!out.containsKey(ri.activityInfo.packageName)) out.put(ri.activityInfo.packageName, ri.activityInfo);
        }
    }

    /** Takes part in the dock, grid order and Move like an app; HomeActivity opens it specially. */
    private static AppInfo fireHomeTile(Context context) {
        Drawable icon = context.getDrawable(R.drawable.icon_firetv_home);
        return new AppInfo(FIRE_HOME, "Fire TV Home", null, icon, sampleColor(icon), new Intent());
    }

    private static AppInfo koiPondTile(Context context) {
        Drawable icon = context.getDrawable(R.drawable.icon_koi_pond);
        Intent pond = new Intent(context, ScreensaverActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return new AppInfo(KOI_POND, "Koi Pond", null, icon, sampleColor(icon), pond);
    }

    /** Null on devices without the mirroring receiver (or with it disabled), so no dead tile. */
    private static AppInfo castTile(Context context) {
        Intent mirror = new Intent().setClassName(CAST_SINK, CAST_SINK_ACTIVITY)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Intent.resolveActivity trusts an explicit component blindly, so ask the package manager
        try {
            ActivityInfo ai = context.getPackageManager().getActivityInfo(mirror.getComponent(), 0);
            if (!ai.exported || !ai.isEnabled() || !ai.applicationInfo.enabled) return null;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        Drawable icon = context.getDrawable(R.drawable.icon_cast);
        return new AppInfo(CAST, "Cast", null, icon, sampleColor(icon), mirror);
    }

    private static AppInfo toApp(PackageManager pm, ActivityInfo ai) {
        try {
            Intent launch = pm.getLeanbackLaunchIntentForPackage(ai.packageName);
            if (launch == null) launch = pm.getLaunchIntentForPackage(ai.packageName);
            if (launch == null) return null;
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            String label = String.valueOf(ai.loadLabel(pm));
            Drawable banner = ai.loadBanner(pm);
            if (banner == null) banner = ai.applicationInfo.loadBanner(pm);
            Drawable icon = ai.loadIcon(pm);
            int glow = sampleColor(banner != null ? banner : icon);
            return new AppInfo(ai.packageName, label, banner, icon, glow, launch);
        } catch (Exception e) {
            return null;
        }
    }

    /** Weighted mean of the colourful pixels, pushed to a bright, saturated glow colour. */
    static int sampleColor(Drawable d) {
        if (d == null) return 0xFF8E8E9E;
        Bitmap bmp = Bitmap.createBitmap(32, 18, Bitmap.Config.ARGB_8888);
        Rect old = new Rect(d.getBounds());
        d.setBounds(0, 0, 32, 18);
        d.draw(new Canvas(bmp));
        d.setBounds(old);
        double sx = 0, sy = 0, ss = 0, wsum = 0;
        float[] hsv = new float[3];
        for (int py = 0; py < 18; py++) {
            for (int px = 0; px < 32; px++) {
                int c = bmp.getPixel(px, py);
                float a = Color.alpha(c) / 255f;
                if (a < 0.5f) continue;
                Color.colorToHSV(c, hsv);
                double w = hsv[1] * hsv[2] * hsv[2] * a; // favour vivid, bright pixels
                double rad = Math.toRadians(hsv[0]);
                sx += Math.cos(rad) * w;
                sy += Math.sin(rad) * w;
                ss += hsv[1] * w;
                wsum += w;
            }
        }
        bmp.recycle();
        if (wsum < 8) return 0xFF8E8E9E; // monochrome banner: soft neutral glow
        float hue = (float) ((Math.toDegrees(Math.atan2(sy, sx)) + 360) % 360);
        float sat = (float) Math.max(0.5, Math.min(0.9, ss / wsum));
        return Color.HSVToColor(new float[] {hue, sat, 0.95f});
    }

    private AppLoader() {}
}
