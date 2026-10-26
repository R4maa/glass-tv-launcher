# Releasing Glass

For the maintainer. Users get releases from [Releases](https://github.com/R4maa/glass-tv-launcher/releases), and Glass
offers each new one in **Settings › Update**.

## The release key

Every release must be signed with the same key, or Android refuses to install it over the previous one and every user
has to uninstall. The key is **not** in this repository:

- `~/.glass-tv/release.jks` (the keystore) and `~/.glass-tv/keystore.properties` (its path and passwords) on the
  maintainer's computer. Gradle reads the properties file, or the file named by the `GLASS_KEYSTORE` environment
  variable.
- Its certificate's SHA-256 is `660e149eb48e2b309434cea43f06b51daa9705cff149ac7b0fb846bcd9c6deb8`
  (`release.sh` checks every build against it).
- **Keep an offline backup of both files** (for example a password manager plus a USB stick). If they're lost, no
  existing user can update again without uninstalling.

Without the key, `assembleRelease` stops with an error instead of quietly signing with the debug key.

## Cutting a release

1. Bump the version in `glass-launcher/launcher/app/build.gradle`: `versionCode` goes up by 1 every release (Android
   only installs a higher one), `versionName` follows [semantic versioning](https://semver.org) (`1.2.0` for new
   features, `1.1.1` for fixes).
2. Add a `## <versionName> · <YYYY-MM-DD>` section at the top of [CHANGELOG.md](CHANGELOG.md). It becomes the release
   notes, so use full `https://github.com/…` links, not relative ones.
3. Commit and push to `main` (`release.sh` refuses to publish uncommitted or unpushed changes, and tags the
   commit it built).
4. From `glass-launcher/`, run `./release.sh --dry-run` and read the notes it prints, then `./release.sh`. It builds,
   checks the signature, and creates the GitHub release `v<versionName>` with `glass.apk` attached.

Glass compares the release tag (without the `v`) with its own `versionName`, and downloads the first `.apk` asset.

## Notes

- The release build is marked debuggable on purpose: `configure.sh` writes Glass's private files with `run-as`, which
  only works on debuggable apps. With ADB debugging off on the stick (as the README advises), nothing else can use that.
