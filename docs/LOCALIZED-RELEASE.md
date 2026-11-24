# EN/FR/AR launcher release

Version `0.8.32-l10n.1` (108) starts the independently signed release line for `ras.dashing.tbox.launcher`. It builds on the localized 0.8.32 source. Both the language choice and the dashboard layout are local to the launcher. Arabic text uses its natural direction and the dashboard keeps its physical left and right positions.

## Moving from the original launcher

Android checks the signing identity when updating an installed package. The independently signed APK keeps the original package name, so migration from the original signer requires a one-time uninstall and fresh install. Uninstalling clears the launcher's private data. For a preinstalled or protected HOME app, confirm with the maintainer that ordinary replacement and recovery are supported on that head unit before starting.

1. While parked, open **Backup & Restore → Save backup** in the original launcher. In `Downloads/DCLbackups`, verify that the new `dcl_backup_*.json` is present and nonempty. Copy the JSON and its matching light and dark wallpaper JPEGs, if present, to private storage outside the head unit. The export includes preferences and can contain pairing credentials.
2. Have a working alternative HOME launcher and the original signed APK available. Select the alternative HOME and confirm that you can open Android settings and the installer from it before removing the original launcher.
3. Use the normal Android app-management and installer screens for the one-time replacement. Grant the permissions and special access the launcher needs. Select the new launcher as HOME after confirming it opens.
4. Put the backup files together in `Downloads/DCLbackups`. Open **Backup & Restore → Restore settings from file**, select the JSON in the **DCLbackups** list, then choose **Restart launcher**. Check the layout, saved presets, wallpaper and language choice. An original build without the language selector has no saved language choice to import.

The backup code exports SharedPreferences, launcher DataStore settings and available custom wallpaper files. Android controls HOME selection, runtime permissions, special access and external widget bindings separately. Check these after migration and reselect or reauthorize them where needed. OEM permissions or services tied to the original certificate may require maintainer support. The backup does not contain other apps or their data.

Future releases can update this personal build in place and retain app data. Keep the package name, reuse the same release key, and assign a higher version code for each release. Returning to the original signing identity requires the same backup and fresh-install process.

## Building a subsequent release

Build with the repository's Gradle wrapper using `:launcher:assembleEnRelease :launcher:testEnDebugUnitTest` and set the next release code with `-PlauncherVersionCode=109` or higher. Update the displayed personal version in `launcher/build.gradle.kts` when cutting the next release. Sign the resulting aligned APK with the persistent private keystore using Android SDK `apksigner sign --ks <private-keystore> --out <signed.apk> <unsigned.apk>`. Verify the result with `apksigner verify --verbose --print-certs <signed.apk>` and compare the signer SHA-256 fingerprint with the first personal release. Keep the encrypted keystore and a recoverable copy of its password in private backups.

The personal build ships with its update feed unconfigured. If the fork operates its own compatible OTA feed later, set `launcher.personalUpdate.releasePublicKey` and `launcher.personalUpdate.devPublicKey` in the ignored `local.properties`. Only offer APKs signed with this personal release key.

## Checks before vehicle use

- Have French and Arabic speakers review terminology, warnings, clipped labels, mixed Arabic and Latin text, numbers and units on the target display.
- On a parked head unit, verify cold start, HOME selection, persistence after restart, widget and navigation access, and restored settings. Check that camera views, seat and mirror controls, and left/right indicators still correspond to their physical sides. Confirm access to OEM vehicle services with the new signer.
