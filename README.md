# NeverSoft 11 — a Windows 11-style Android launcher

A native **Kotlin + Jetpack Compose** home-screen launcher that skins Android to
look and behave like Windows 11, fully rebranded to **NeverSoft Services**, with
a CMD-styled **Command Prompt** (Termux). Built to the *NeverSoft 11 Engineer
Loop* doctrine.

> **Reality lock.** This is a *skin/launcher over Android* — no NT kernel, no
> `.exe`, no registry. Everything visual and behavioural targets Windows 11
> parity; the substrate stays Android. Rebranding to NeverSoft Services removes
> Microsoft trademark exposure (remaining licensing items: ship your own system
> font — Selawik/Inter — and your own wallpapers).

---

## ⚠️ Verification boundary (read this first)

This project was scaffolded in a headless Linux cloud container with **no Android
SDK and no device**, and where Google's Maven/SDK hosts are unreachable. So:

| Check | Where it runs | Status |
|---|---|---|
| Compile / `./gradlew assembleDebug` | **GitHub Actions** (`.github/workflows/android-build.yml`) | automated red→green gate |
| Lint / unit tests | GitHub Actions | automated (lint non-blocking) |
| Install + set as default home | **a real phone / redma** | **you** |
| Fidelity score (FS ≥ 95 vs real Win11), 60/120 fps, blur quality | **a real phone / redma** | **you** |

The doctrine's device-fidelity loop (capture → diff vs a live Win11 screenshot →
score) **cannot run in CI** — it needs a screen. Those scores are **not
fabricated here**; they're yours to measure on-device. CI proves it *compiles and
assembles*; your phone proves it *looks/feels identical*.

---

## Build & run

### On redma (Android Studio)
1. Open this folder in Android Studio (Giraffe+; JDK 17).
2. Let Gradle sync (downloads AGP 8.7.3 / Compose BOM 2024.12.01 / SDK 35).
3. Run the `app` config onto a device/emulator (Android 10 / API 29+).

### From the command line
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Set as the home launcher
Settings → Apps → Default apps → Home app → **NeverSoft 11**
(or press Home and pick it). It declares `CATEGORY_HOME` and relaunches after
reboot.

---

## The Command Prompt (Termux, doctrine §3A)

Tier A — skin Termux so it reads as CMD immediately:
```bash
# inside Termux, from a checkout of this repo:
bash termux/install.sh
```
This applies the **Campbell** colour scheme, fullscreen + extra-keys, silences
the welcome message, and installs the NeverSoft banner + a `C:\Users\redma>`
prompt that tracks your real directory. Drop `CascadiaMono.ttf` at
`termux/font.ttf` before installing for the exact CMD font.

The launcher's **Command Prompt** tile (Start menu + desktop) deep-links to
Termux. *Tier B* (a drawn Windows title bar around an embedded terminal view) is
backlog — note its GPLv3 implication for distribution.

---

## Architecture

```
app/src/main/java/com/neversoft/launcher/
  Brand.kt                     # single source of truth for all NeverSoft strings (§1.6)
  MainActivity.kt              # HOME activity, edge-to-edge + immersive
  apps/AppRepository.kt        # query + launch installed apps; Termux deep-link
  ui/theme/Tokens.kt           # Fluent colour/geometry tokens (§1.1–1.3)
  ui/theme/Theme.kt            # Compose theme (dark)
  ui/modifier/Acrylic.kt       # acrylic tint + RenderEffect blur (API 31+) (§1.2)
  ui/Shell.kt                  # root: wallpaper → desktop → overlays → taskbar
  ui/Desktop.kt                # desktop shortcut grid
  ui/Taskbar.kt                # centered cluster, Start logo, tray, clock (§1.4)
  ui/StartMenu.kt              # search, pinned grid, user + power (§1.4)
  ui/Flyouts.kt                # Quick Settings + Notification Center + calendar
  ui/components/AppGlyph.kt    # app icon / fallback
termux/                        # CMD skin configs + installer (§3A)
.github/workflows/             # the CI build gate
```

## Fidelity backlog (doctrine §2.1 — gate each at FS ≥ 95 on-device)
1. ✅ Theme engine + tokens · 2. ✅ Desktop surface · 3. ✅ Taskbar · 4. ✅ Tray +
Quick Settings + Notification Center · 5. ✅ Start menu · 6. ✅ App launch ·
7. ✅ CMD skin (Tier A) · 8. ◻ Context menus / Snap Layouts / Widgets / Task View ·
9. ◻ Lock screen (cosmetic, §5) · 10. ◻ Optimization pass (§6).
Freeform windowing needs `enable_freeform_support` via ADB/Shizuku (§0, §5).

## Luxury / polish extras already wired
- Single-constant rebrand (`Brand.kt`) incl. a "keep the word Windows" toggle.
- Acrylic **depth tiers**: taskbar (thin) < flyouts (thick) < Start (thickest).
- Real `RenderEffect` blur path (`Modifier.blurLayer`, API 31+) with graceful
  no-op fallback below 12.
- Running/active **pill indicator** + hover highlight on taskbar buttons.
- Live tray clock + a real month **calendar** with today highlighted.
- Stateful Quick Settings toggles + brightness/volume sliders.
- Campbell terminal scheme + dynamic `C:\…>` prompt mapping.

## Known gaps (doctrine §5)
Top status bar can't be fully restyled without root → hidden via immersive; we
draw our own bottom tray. True per-app floating windows are OEM-dependent
(Shizuku/DeX). Real `.exe` is out of scope. Lock screen / boot animation are
root-only; only cosmetic versions are feasible.
