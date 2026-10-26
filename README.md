<div align="center">

# 🎏 Glass TV

### A calmer Fire TV Stick

No ads, no tracking, a clean home screen, and a koi pond that swims across your TV when it's idle.

[![Fire TV](https://img.shields.io/badge/Fire_TV-Stick-FF9900?style=flat-square)](#what-you-need)
[![Android 9+](https://img.shields.io/badge/Android-9%2B_(API_28)-3DDC84?style=flat-square&logo=android&logoColor=white)](glass-launcher/launcher)
[![No root](https://img.shields.io/badge/root-not_needed-blue?style=flat-square)](#undoing-everything)
[![Licence](https://img.shields.io/badge/licence-MIT_%2B_PolyForm_NC-lightgrey?style=flat-square)](#credits-and-licences)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-support_me-FF5E5B?style=flat-square&logo=ko-fi&logoColor=white)](https://ko-fi.com/rama2019)

[**Features**](#-features) · [**Let your agent do it**](#-let-your-agent-do-it) · [**Manual setup**](#manual-setup) · [**Changelog**](CHANGELOG.md) · [**Undo**](#undoing-everything) · [**Credits**](#credits-and-licences)

<br>

<img src="docs/home.png" alt="Glass home screen: clock, Now Playing pill, Wi-Fi and Settings, with the app dock below" width="900">

</div>

<br>

Glass TV turns a stock Fire TV Stick into something that feels like a nice piece of furniture instead of a billboard.
It builds on [**Fire TV Glass**](https://github.com/ivantrj/firetv-glass-launcher) by ivantrj, adds a Now Playing pill,
tiles you can rearrange and a Settings menu that behaves, and brings in the [**nagomi**](https://github.com/msk1039/nagomi)
koi pond as a screensaver that follows the real time of day and the weather outside.

> [!NOTE]
> **No root, nothing uninstalled.** Amazon's packages are only disabled, and one script turns everything back on.

## 🙏 Built on the work of others

This project wouldn't exist without two open-source projects. **Please go give them a ⭐.**

<table>
  <tr>
    <td width="50%" valign="top">
      <h3>📺 <a href="https://github.com/ivantrj/firetv-glass-launcher">Fire TV Glass</a></h3>
      <p>by <a href="https://github.com/ivantrj">ivantrj</a> · MIT</p>
      <p>The Glass launcher itself, the debloat list and the install/restore scripts. Everything in
      <a href="glass-launcher"><code>glass-launcher/</code></a> is a fork of it.</p>
    </td>
    <td width="50%" valign="top">
      <h3>🎏 <a href="https://github.com/msk1039/nagomi">nagomi</a></h3>
      <p>by <a href="https://github.com/msk1039">Mayank Kadam (msk1039)</a> · PolyForm Noncommercial 1.0.0</p>
      <p>The koi pond: the fish, water, lily pads and weather. <a href="screensaver/nagomi"><code>screensaver/nagomi/</code></a>
      is a copy of it with a TV screensaver mode added.</p>
    </td>
  </tr>
</table>

Glass TV adds the Now Playing pill, tile moving, the Settings menu, Home handling and the screensaver integration on top.

## ✨ Features

<table>
  <tr>
    <td width="50%" valign="top">
      <h4>🏠 A home screen that stays yours</h4>
      The Home button always opens Glass, even from inside Fire TV Settings or Netflix. A <b>Fire TV Home</b> tile
      opens Amazon's home for the times you actually want it.
    </td>
    <td width="50%" valign="top">
      <h4>🎵 Now Playing pill</h4>
      Album art and "Title · Artist" for whatever is playing (Spotify, YouTube, …). Select pauses and resumes,
      Menu (☰) opens the app. Dimmed with a pause badge while paused.
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h4>↔️ Rearrange your tiles</h4>
      Menu (☰) on any tile → <i>Move</i>, then use the arrows. Up from the grid puts an app in the four-slot dock;
      OK places it, Back cancels.
      <br><br>
      <img src="docs/move-mode.png" alt="Move mode: a tile carried with a blue rim and an instruction bar">
    </td>
    <td width="50%" valign="top">
      <h4>⚙️ A Settings menu that works</h4>
      Jumps straight into Fire TV Settings (stock Fire OS bounces you through Amazon's home first) and holds the
      screensaver options.
      <br><br>
      <img src="docs/screensaver-menu.png" alt="Settings → Screensaver submenu with idle times and Preview now">
    </td>
  </tr>
</table>

### 🎏 Koi pond screensaver

Starts after 2 to 30 idle minutes (your choice, or off) and never while something is playing. Every start is a new
pond with different fish, lily pads and flowers, and one time in twenty a **giant gold or platinum koi**. The sky
follows your local sunrise, sunset and weather. Any key closes it, and a **Koi Pond** tile starts it whenever you like.

<table>
  <tr>
    <td><img src="docs/pond-sunny.jpg" alt="Koi pond on a sunny day"><p align="center"><sub>☀️ Sunny</sub></p></td>
    <td><img src="docs/pond-sunset.jpg" alt="Koi pond at sunset"><p align="center"><sub>🌇 Sunset</sub></p></td>
  </tr>
  <tr>
    <td><img src="docs/pond-mist.jpg" alt="Koi pond in the mist"><p align="center"><sub>🌫️ Mist</sub></p></td>
    <td><img src="docs/pond-moonlight-rain.jpg" alt="Koi pond by moonlight in the rain"><p align="center"><sub>🌧️ Moonlight and rain</sub></p></td>
  </tr>
</table>

### 🧹 Debloat (optional)

Switches off Amazon's telemetry, ad and recommendation services and preloaded apps. Every change can be undone.

### 🔄 Updates

Glass checks [GitHub Releases](https://github.com/R4maa/glass-tv-launcher/releases) once a day. When there's a new
version, the Settings pill reads **Settings · Update** and **Settings › Update to Glass x.y.z** installs it (Fire OS asks you
to confirm first). Don't want that? [Turn the check off during setup](#turn-off-updates), or later in
**Settings › Updates**. With it off, Glass only looks for updates when you choose **Check now**. What changed in each
version is in the [changelog](CHANGELOG.md).

## What you need

| | |
|---|---|
| 📺 **Fire TV Stick** | Fire OS 7 or newer (Android 9+). Built and tested on a Fire TV Stick 4K Max (2nd gen, Fire OS 8.1); see [Compatibility](#compatibility) |
| 💻 **A computer** | On the same Wi-Fi as the stick, with `adb` ([Android platform-tools](https://developer.android.com/tools/releases/platform-tools): unzip it and add the folder to your PATH, or type the full path to `adb` in the commands below). Building Glass yourself also needs **git**, **JDK 17+** and the **Android SDK** |
| 🐍 **Python 3** | Only for upstream's debloat shell scripts (macOS/Linux) |

### Compatibility

| Fire OS | Android | Status |
|---|---|---|
| **8.x** | 11 (API 30) | ✅ Tested on a Fire TV Stick 4K Max (2nd gen, Fire OS 8.1) |
| **7.x** | 9 (API 28) | 🧪 Installs, but untested on a real stick. The home screen, app grid and app launching work on an Android 9 TV emulator; the Home-button takeover, the Fire TV Home tile and the koi pond (needs WebGL from the stick's WebView) haven't been checked on Fire OS 7 hardware |
| **6.x and older** | 7.1 and below | ❌ Won't install |

Other Android TV / Google TV devices on Android 9+ can install it too, but they're untested, and the Fire TV–specific parts (Home button, Fire TV Home tile, debloat scripts) won't apply. If you try it on another stick or Fire OS version, please [open an issue](https://github.com/R4maa/glass-tv-launcher/issues) and say what worked.

<a id="on-the-tv"></a>

### 📺 First, on the TV (everyone does this)

1. **Settings › My Fire TV › About**: click the device name **7 times** to unlock Developer Options.
2. **Settings › My Fire TV › Developer Options**: turn on **ADB debugging**.
3. **Settings › My Fire TV › About › Network**: write down the **IP address**.

## 🤖 Let your agent do it

Got a coding agent with a terminal, like **Claude Code**, **Codex** or **Cursor**? It can do the whole install for
you while you hold the remote.

<table>
  <tr>
    <td align="center" width="33%"><h3>1️⃣</h3>Do the <a href="#on-the-tv">three steps on the TV</a> above</td>
    <td align="center" width="33%"><h3>2️⃣</h3>Copy the prompt below into your agent</td>
    <td align="center" width="33%"><h3>3️⃣</h3>Keep the remote handy and answer its questions</td>
  </tr>
</table>

```text
Install Glass TV (https://github.com/R4maa/glass-tv-launcher) on my Fire TV Stick.
Follow the repo's README; it is the source of truth. Work step by step and tell me what
you're doing as you go.

1. Ask me for my Fire TV Stick's IP address (Settings > My Fire TV > About > Network). I
   have already turned on ADB debugging. Don't continue until I've given you the IP.
2. Check my computer for adb (Android platform-tools). If it's missing, tell me how to
   install it for my OS, and ask before you install anything.
3. Download the latest glass.apk from
   https://github.com/R4maa/glass-tv-launcher/releases/latest/download/glass.apk
   into my Downloads folder (or another folder you tell me), and run the adb commands
   below from that folder.
4. Run "adb connect <IP>:5555". Tell me to look at the TV, tick "Always allow" and
   accept the "Allow USB debugging?" prompt. Wait for me, then check that "adb devices"
   lists the stick as "device", not "unauthorized".
5. Install glass.apk with "adb -s <IP>:5555 install -r -g glass.apk". If that fails
   with INSTALL_FAILED_UPDATE_INCOMPATIBLE, an older self-built Glass is installed: tell
   me, and only after I agree, uninstall it and install again. Then run the four commands
   from the README's "Install it on the stick" step (two for the Home button service,
   one for Now Playing access, one that lets Glass install its own updates).
6. Ask me to press Home on the remote and confirm that Glass opens.
7. Tell me Glass checks GitHub once a day for a new version and offers it in its
   Settings menu, and ask: "Keep the daily update check on? (yes = keep it, no = turn
   it off)". If I say no, run
   "adb -s <IP>:5555 shell am start -n com.ivantrj.glasslauncher/.HomeActivity --ez updateChecks false"
   (README Manual setup step 3).
8. Ask whether I want Glass's koi pond as the screensaver. If yes, tell me to set Fire
   TV Settings > Display & Sounds > Screensaver > Start Time to Never. Once I have, run
   the two commands from the README's screensaver step that restore the 15-minute sleep
   timeout.
9. Do NOT run any debloat script or disable any packages unless I explicitly ask. If I
   ask, show me glass-launcher/debloat/disable-list.txt first and let me remove anything
   I use.
10. When you're done, summarise what changed and how to undo it (README "Undoing
   everything"). Remind me to turn off ADB debugging in Developer Options.

Never factory-reset, uninstall Amazon apps, or change settings other than the ones
listed here.
```

> [!TIP]
> Hover over the box and click the copy button in its top-right corner to copy the whole prompt.

<details>
<summary><b>🛡️ What the agent will and won't do</b></summary>

<br>

| ✅ Will | 🚫 Won't |
|---|---|
| Ask for your stick's IP before touching anything | Install missing tools without asking you first |
| Download the latest release of Glass | Uninstall an older Glass without asking you |
| Wait while you accept the debugging prompt on the TV | Disable any Amazon packages unless you ask |
| Install Glass and turn on its permissions | Uninstall Amazon apps or factory-reset the stick |
| Check with you that Home opens Glass | Change settings beyond the ones in the prompt |
| Ask whether Glass should check for updates | Turn update checks off unless you say so |
| Finish with a summary and how to undo it | Leave you without a reminder to switch ADB debugging off |

</details>

## Manual setup

Prefer to do it yourself? Do the [three steps on the TV](#on-the-tv) first, then:

<details open>
<summary><b>1 · Download Glass</b></summary>

<br>

Download **[glass.apk](https://github.com/R4maa/glass-tv-launcher/releases/latest/download/glass.apk)** from the
[latest release](https://github.com/R4maa/glass-tv-launcher/releases/latest). Your browser saves it in your
**Downloads** folder; open a terminal there, so the commands below can find it:

```powershell
cd $HOME\Downloads   # Windows (PowerShell)
```

```bash
cd ~/Downloads       # macOS / Linux
```

</details>

<details>
<summary><b>…or build it yourself</b></summary>

<br>

A copy you build is signed with your own key, so it can't install updates from Releases: it would offer them and then
fail to install them. Turn its update check off with the command in step 3, and to get updates later, uninstall it and
install `glass.apk` instead. The same error appears the other way round too: installing your own build over the
release fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE` until you uninstall Glass.

On macOS / Linux, [`glass-launcher/install.sh`](glass-launcher/install.sh) builds and installs in one go; it asks about
update checks (off unless you say yes), or pass `--updates` / `--no-updates`.

**Windows (PowerShell)**

```powershell
git clone https://github.com/R4maa/glass-tv-launcher.git
cd glass-tv-launcher\glass-launcher\launcher
"sdk.dir=$($env:LOCALAPPDATA -replace '\\','/')/Android/Sdk" | Set-Content local.properties -Encoding ascii
.\gradlew.bat assembleDebug
```

**macOS / Linux**

```bash
git clone https://github.com/R4maa/glass-tv-launcher.git
cd glass-tv-launcher/glass-launcher/launcher
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties   # or wherever your SDK lives
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`; use it in place of `glass.apk` below. The koi pond is
already built into the app's assets, so you don't need Node unless you change the pond.

</details>

<details open>
<summary><b>2 · Install it on the stick</b></summary>

<br>

Replace `192.168.1.50` with your stick's IP.

```bash
adb connect 192.168.1.50:5555
adb -s 192.168.1.50:5555 install -r -g glass.apk
```

> ⚠️ **Watch the TV:** the first connection makes it ask **Allow USB debugging?** Tick **Always allow** and accept, or
> `adb` will show the stick as `unauthorized`.

> [!NOTE]
> **`INSTALL_FAILED_UPDATE_INCOMPATIBLE`?** You have an older Glass that was built from source. Uninstall it first
> (`adb -s 192.168.1.50:5555 uninstall com.ivantrj.glasslauncher`; your dock order and Glass settings start fresh), then
> run the install again. If you had turned update checks off, do step 3 again too: uninstalling turns them back on.

Then give Glass the permissions it needs, once:

```bash
adb -s 192.168.1.50:5555 shell settings put secure enabled_accessibility_services com.ivantrj.glasslauncher/com.ivantrj.glasslauncher.HomeButtonService
adb -s 192.168.1.50:5555 shell settings put secure accessibility_enabled 1
adb -s 192.168.1.50:5555 shell cmd notification allow_listener com.ivantrj.glasslauncher/com.ivantrj.glasslauncher.MediaListener
adb -s 192.168.1.50:5555 shell appops set com.ivantrj.glasslauncher REQUEST_INSTALL_PACKAGES allow
```

- **Glass Home button** (an accessibility service) is how Glass takes over the Home button. Fire OS doesn't let you
  pick another launcher, so this is the standard workaround.
- **Notification access** lets the Now Playing pill read other apps' media sessions.
- **Install unknown apps** lets Glass install its own updates. Fire OS still asks you before each one.

Press Home on the remote: you should land in Glass. 🎉

</details>

<a id="turn-off-updates"></a>

<details open>
<summary><b>3 · Updates: keep or turn off</b></summary>

<br>

Glass checks GitHub once a day for a new version and offers it under **Settings** (see [Updates](#-updates)).
**To keep that, do nothing.** To turn it off, run this; the TV switches to Glass and confirms it:

```bash
adb -s 192.168.1.50:5555 shell am start -n com.ivantrj.glasslauncher/.HomeActivity --ez updateChecks false
```

The TV shows **✓ Glass won't check for updates · Settings › Updates to change**. To switch it back on later, choose
**Settings › Updates › Check daily** on the TV, or run the same command with `true`:

```bash
adb -s 192.168.1.50:5555 shell am start -n com.ivantrj.glasslauncher/.HomeActivity --ez updateChecks true
```

Uninstalling Glass resets this to on, so run the command again if you reinstall.

</details>

<details>
<summary><b>4 · Let Glass be the screensaver (optional)</b></summary>

<br>

In **Fire TV Settings › Display & Sounds › Screensaver**, set **Start Time** to **Never**, then choose the idle time in
Glass's own **Settings › Screensaver** menu. Picking *Never* also tells Fire OS never to sleep; to keep the normal
15-minute sleep, run:

```bash
adb -s 192.168.1.50:5555 shell settings put system screen_off_timeout 900000
adb -s 192.168.1.50:5555 shell settings put secure contextual_screen_off_timeout 900000
```

</details>

<details>
<summary><b>5 · Debloat (optional)</b></summary>

<br>

[`glass-launcher/debloat.sh`](glass-launcher/debloat.sh) disables the packages listed, with notes, in
[`glass-launcher/debloat/disable-list.txt`](glass-launcher/debloat/disable-list.txt). Read the list and remove anything
you use before running it. See the [upstream README](glass-launcher/README.md) for the full walkthrough.

</details>

## Undoing everything

| Where | How |
|---|---|
| 📺 **On the TV** | The **Fire TV Home** tile opens Amazon's home. To stop Glass taking Home entirely: **Fire TV Settings › Accessibility › Glass Home button › Off** |
| 🪟 **Windows** | [`firestick-restore.ps1`](firestick-restore.ps1) re-enables the disabled packages, turns Amazon's screensaver back on and gives Home back to Amazon |
| 🍎 **macOS / Linux** | [`glass-launcher/restore.sh`](glass-launcher/restore.sh) |

```powershell
.\firestick-restore.ps1 -Stick 192.168.1.50          # everything
.\firestick-restore.ps1 -Stick 192.168.1.50 -Glass   # just give Home back
```

> [!WARNING]
> Switch **ADB debugging** off again in Developer Options when you're done.

## Good to know

- 🌦️ The pond looks up your rough location from your IP ([ipwho.is](https://ipwho.is)) and the weather from
  [Open-Meteo](https://open-meteo.com). Offline, it falls back to the clock.
- 🐢 On a Fire TV Stick 4K Max the pond runs at about 20–23 fps (target 30).
- 📢 Amazon's sponsored row and hero banner come from Amazon's servers and can't be removed by debloating; with Glass
  as your home screen you just don't see them.
- 🔒 Some Amazon packages are "protected" and can't be disabled, and a Fire OS update may switch disabled ones back on.
- 🔑 Releases are signed with the project's key, so new versions install over the old one. A copy you build yourself
  is signed with your own key and can't take updates from Releases.
- 🐞 Release builds are marked *debuggable* so `configure.sh` can write Glass's settings over adb. That only matters
  while ADB debugging is on, which is one more reason to switch it off when you're done.

<details>
<summary><b>📁 Repository layout</b></summary>

<br>

| Path | What it is |
|---|---|
| [`glass-launcher/`](glass-launcher) | The launcher: Android project (Java, Gradle) plus upstream's debloat/install scripts |
| [`screensaver/nagomi/`](screensaver/nagomi) | The koi pond, a vendored copy of [nagomi](https://github.com/msk1039/nagomi) with a `?tv=1` screensaver mode ([changes](screensaver/nagomi/GLASS-CHANGES.md)) |
| [`screensaver/build.ps1`](screensaver/build.ps1) | Rebuilds the pond into the launcher's assets (needs Node; only after editing the pond) |
| [`demo-player/`](demo-player) | A tiny fake music player for testing Now Playing in an emulator, where Spotify won't run |
| [`firestick-restore.ps1`](firestick-restore.ps1) | Undo script for Windows |
| [`CHANGELOG.md`](CHANGELOG.md) | What changed in each release |
| [`RELEASING.md`](RELEASING.md) | How a release is built, signed and published (maintainer) |

</details>

## ☕ Support

If Glass TV makes your TV a little calmer, you can [buy me a coffee on Ko-fi](https://ko-fi.com/rama2019).

## Credits and licences

- **[Fire TV Glass](https://github.com/ivantrj/firetv-glass-launcher)** by [ivantrj](https://github.com/ivantrj),
  MIT, Copyright (c) 2026 ivantrj ([licence](glass-launcher/LICENSE)). Forked at upstream commit `95523e5`; the
  launcher, its design, the debloat list and the scripts are theirs, and this repo's changes build on them.
- **[nagomi](https://github.com/msk1039/nagomi)** by [Mayank Kadam (msk1039)](https://github.com/msk1039),
  **PolyForm Noncommercial 1.0.0** ([licence](screensaver/nagomi/LICENSE)). Vendored at commit `01e93a4`; the
  changes made for the TV are listed in [GLASS-CHANGES.md](screensaver/nagomi/GLASS-CHANGES.md). The pond, and any
  build that includes it, may only be used for non-commercial purposes.

  Required Notice: Copyright 2026 Mayank Kadam (https://github.com/msk1039)
- **Outfit** font, SIL Open Font License ([licence](glass-launcher/launcher/app/src/main/assets/fonts/OFL.txt)).

<div align="center">
<br>
<sub>Not affiliated with Amazon. Fire TV is a trademark of Amazon.com, Inc.</sub>
</div>
