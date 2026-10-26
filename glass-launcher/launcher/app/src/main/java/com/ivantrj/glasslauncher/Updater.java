package com.ivantrj.glasslauncher;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Updates from the project's GitHub Releases. When checks are on (the default; setup or
 * Settings › Updates can turn them off) Glass asks GitHub once a day for the latest release.
 * A newer one is only offered: installing it is one press in the Settings menu, and Fire OS
 * still shows its own confirm screen. With checks off Glass makes no update requests at all.
 */
final class Updater {

    private static final String TAG = "Updater";
    static final String REPO = "R4maa/glass-tv-launcher";
    private static final String LATEST = "https://api.github.com/repos/" + REPO + "/releases/latest";
    private static final long CHECK_EVERY = 24 * 3600_000L, RETRY_AFTER = 3600_000L;

    /** A release newer than this build. */
    static final class Release {
        final String version, notes, apkUrl;

        Release(String version, String notes, String apkUrl) {
            this.version = version;
            this.notes = notes;
            this.apkUrl = apkUrl;
        }
    }

    enum Check { NEWER, UP_TO_DATE, FAILED }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("home", Context.MODE_PRIVATE);
    }

    static boolean enabled(Context context) {
        return prefs(context).getBoolean("updateChecks", true);
    }

    static void setEnabled(Context context, boolean on) {
        SharedPreferences.Editor e = prefs(context).edit().putBoolean("updateChecks", on);
        if (!on) e.remove("updateVersion").remove("updateNotes").remove("updateUrl");
        e.apply();
    }

    static String currentVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /** The newer release found by the last check (daily, or Check now with checks off), or null. */
    static Release available(Context context) {
        SharedPreferences p = prefs(context);
        String version = p.getString("updateVersion", null);
        if (version == null || compare(version, currentVersion()) <= 0) return null;
        return new Release(version, p.getString("updateNotes", ""), p.getString("updateUrl", null));
    }

    /** True once a day while checks are on; after a failed check (offline), an hour later. */
    static boolean due(Context context) {
        SharedPreferences p = prefs(context);
        long now = System.currentTimeMillis();
        return enabled(context) && now - p.getLong("updateCheckedAt", 0) > CHECK_EVERY
                && now - p.getLong("updateTriedAt", 0) > RETRY_AFTER;
    }

    /** Asks GitHub for the latest release. Blocking: call off the main thread. */
    static Check check(Context context) {
        prefs(context).edit().putLong("updateTriedAt", System.currentTimeMillis()).apply();
        try {
            JSONObject release = new JSONObject(get(LATEST));
            String version = release.getString("tag_name").replaceFirst("^[vV]", "");
            String apkUrl = null;
            JSONArray assets = release.optJSONArray("assets");
            for (int i = 0; assets != null && i < assets.length(); i++) {
                JSONObject asset = assets.getJSONObject(i);
                if (asset.getString("name").endsWith(".apk")) apkUrl = asset.getString("browser_download_url");
            }
            SharedPreferences.Editor e = prefs(context).edit().putLong("updateCheckedAt", System.currentTimeMillis());
            if (apkUrl != null && compare(version, currentVersion()) > 0) {
                e.putString("updateVersion", version)
                        .putString("updateNotes", firstNote(release.optString("body")))
                        .putString("updateUrl", apkUrl).apply();
                return Check.NEWER;
            }
            e.remove("updateVersion").remove("updateNotes").remove("updateUrl").apply();
            return Check.UP_TO_DATE;
        } catch (java.io.FileNotFoundException e) { // 404: the repo has no releases yet
            prefs(context).edit().putLong("updateCheckedAt", System.currentTimeMillis()).apply();
            return Check.UP_TO_DATE;
        } catch (Exception e) {
            Log.w(TAG, "update check failed: " + e);
            return Check.FAILED;
        }
    }

    /**
     * Downloads the release straight into a package installer session and commits it. Fire OS
     * then shows its install screen; {@link InstallResult} hears how it went. Blocking: call off
     * the main thread. Throws if the download or the session fails.
     */
    static void install(Context context, Release release, java.util.function.IntConsumer percent) throws Exception {
        PackageInstaller installer = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params =
                new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(context.getPackageName());
        int id = installer.createSession(params);
        try (PackageInstaller.Session session = installer.openSession(id)) {
            HttpURLConnection c = (HttpURLConnection) new URL(release.apkUrl).openConnection();
            c.setConnectTimeout(10_000);
            c.setReadTimeout(30_000);
            long size = c.getContentLengthLong();
            try (InputStream in = c.getInputStream(); OutputStream out = session.openWrite("glass.apk", 0, size)) {
                byte[] buf = new byte[64 * 1024];
                long done = 0;
                int shown = -1;
                for (int n; (n = in.read(buf)) > 0; ) {
                    out.write(buf, 0, n);
                    done += n;
                    int now = size > 0 ? (int) (done * 100 / size) / 10 * 10 : -1; // in steps of 10 %
                    if (now != shown) percent.accept(shown = now);
                }
                session.fsync(out);
            }
            Intent result = new Intent(context, InstallResult.class).putExtra("version", release.version);
            PendingIntent sender = PendingIntent.getBroadcast(context, id, result, PendingIntent.FLAG_UPDATE_CURRENT);
            session.commit(sender.getIntentSender());
        } catch (Exception e) {
            installer.abandonSession(id);
            throw e;
        }
    }

    /** The installer's answer: show Fire OS's confirm screen, or tell Glass why it failed. */
    public static final class InstallResult extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                Intent confirm = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                if (confirm != null) context.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return;
            }
            if (status == PackageInstaller.STATUS_SUCCESS) return; // Glass restarts as the new version
            String why;
            if (status == PackageInstaller.STATUS_FAILURE_ABORTED) why = "Update cancelled";
            else if (status == PackageInstaller.STATUS_FAILURE_CONFLICT
                    || status == PackageInstaller.STATUS_FAILURE_INCOMPATIBLE) {
                // A copy built from source is signed with another key: Android won't replace it
                why = "This copy of Glass was built from source, so it can't update itself. Install glass.apk from GitHub once";
            } else if (status == PackageInstaller.STATUS_FAILURE_STORAGE) why = "Not enough space for the update";
            else why = "The update didn't install";
            Log.w(TAG, "install failed: " + status + " " + intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
            context.startActivity(new Intent(context, HomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("updateFailed", why));
        }
    }

    /** -1, 0 or 1 as version a is older, the same or newer than b ("1.10.0" > "1.9.2"). */
    static int compare(String a, String b) {
        String[] x = a.split("\\."), y = b.split("\\.");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int p = i < x.length ? number(x[i]) : 0, q = i < y.length ? number(y[i]) : 0;
            if (p != q) return p < q ? -1 : 1;
        }
        return 0;
    }

    private static int number(String part) {
        String digits = part.replaceAll("\\D.*", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    /** The first line of the release notes, without Markdown list or heading marks. */
    private static String firstNote(String body) {
        for (String line : body.split("\\r?\\n")) {
            if (line.trim().startsWith("#")) continue; // a heading such as "### Fixed"
            String text = line.replaceFirst("^\\s*([-*#]+\\s*)+", "").replace("**", "").trim();
            if (!text.isEmpty()) return text;
        }
        return "";
    }

    private static String get(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        c.setRequestProperty("Accept", "application/vnd.github+json");
        try (InputStream in = c.getInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
            return out.toString("UTF-8");
        }
    }

    private Updater() {}
}
