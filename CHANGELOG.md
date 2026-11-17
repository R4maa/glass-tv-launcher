# Changelog

What changed in each Glass TV release. Download the latest `glass.apk` from
[Releases](https://github.com/R4maa/glass-tv-launcher/releases/latest); Glass also tells you when there's a new one
(see [Updates](https://github.com/R4maa/glass-tv-launcher#-updates)).

## 1.2.1 · 2026-11-17

### Fixed

- **Turning the TV on showed Amazon's home instead of Glass, and Home didn't bring Glass back.** When Fire OS updates
  its built-in web browser engine (it does this overnight, by itself), it stops every app that uses it, Glass
  included. Android then treats the Glass Home button service as crashed and never starts it again. Glass now switches
  the service back on by itself a few seconds later, and opens if Amazon's home is on screen at that moment or the next
  time the TV comes on. If you switched the service off yourself, Glass leaves it off.

  This needs one new permission, which an update can't grant. Run this once (`install.sh` now does it for new
  installs):

  ```bash
  adb shell pm grant com.ivantrj.glasslauncher android.permission.WRITE_SECURE_SETTINGS
  ```

  Without it, Glass works as before. If Home stops opening Glass, switch **Fire TV Settings › Accessibility › Glass
  Home button** off and on again.

## 1.2.0 · 2026-11-09

### Added

- **Cast tile.** One click opens Fire TV's screen-mirroring receiver, so a phone or laptop can mirror to the TV
  without digging through Amazon's menus (Samsung: Smart View; Windows 11: `Win+K`). The tile only appears on devices
  that have the receiver. For iPhones, install an AirPlay receiver app such as AirScreen; the Cast tile's menu (☰)
  opens it.
- The koi pond screensaver stays off while a phone or laptop is being mirrored.

### Changed

- `debloat.sh` no longer disables Miracast screen mirroring, because the Cast tile uses it.

## 1.1.2 · 2026-11-03

### Added

- **Fire OS 6 support (experimental).** Glass now installs on Fire OS 6 sticks (Android 7.1). It's untested on real
  Fire OS 6 hardware, so please [open an issue](https://github.com/R4maa/glass-tv-launcher/issues) and say what works.
  On Fire OS 6 the Outfit font shows at one weight, with bold where Glass uses heavier text.

## 1.1.1 · 2026-10-25

### Fixed

- After Glass updated itself, the TV showed Amazon's home instead of Glass until you pressed Home. Glass now opens
  again by itself as soon as the update is installed, starting with this one.
- Turning the TV on sometimes left it on Amazon's home. After the stick restarts (Fire OS updates, a power cut), Glass's
  Home button service and Amazon's home start at the same moment, and when Amazon's home got there first nothing
  switched it to Glass. Glass now opens itself at start-up, and also whenever the screen comes on with Amazon's home
  still showing.

## 1.1.0 · 2026-10-25

The first release with a ready-made APK, so you no longer need to build Glass yourself.

> [!IMPORTANT]
> **Installed Glass before this release?** Your copy was built on your own computer and signed with your own key, so
> Android won't let the release replace it. Uninstall it once
> (`adb -s 192.168.1.50:5555 uninstall com.ivantrj.glasslauncher`, with your stick's IP), then follow the
> [Manual setup](https://github.com/R4maa/glass-tv-launcher#manual-setup) from step 1. Your dock order and Glass
> settings start fresh. Every update after this one installs over the top.

### Added

- **Updates.** Glass checks GitHub once a day for a newer release. When there is one, the Settings pill reads
  **Settings · Update**, and **Settings › Update to Glass x.y.z** installs it after your TV asks you to confirm.
  **Settings › Updates** turns the daily check off or runs one now; with it off, Glass only looks when you choose
  **Check now**. Setup offers to turn the check off.
- Releases are signed with the project's own key, so later versions install over this one.

### Fixed

- After you opened the **Fire TV Home** tile, the Home button and switching the TV on could keep showing Amazon's home
  instead of Glass. Opening any other app or Fire TV Settings now brings Home back to Glass.

### Already in Glass

Everything from before releases began: the Home button opens Glass, the Fire TV Home tile, the Now Playing pill,
rearranging tiles, the Settings menu, and the koi pond screensaver (real time of day and weather, plus a Koi Pond tile to
start it any time). Glass also installs on Fire OS 7 (Android 9), though it hasn't been tried on a real Fire OS 7 stick
yet; see [Compatibility](https://github.com/R4maa/glass-tv-launcher#compatibility).
