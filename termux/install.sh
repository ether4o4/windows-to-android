#!/data/data/com.termux/files/usr/bin/bash
# NeverSoft Command Prompt installer — run INSIDE Termux (doctrine §3A, Tier A).
#   curl -L <raw>/termux/install.sh | bash      (or copy this repo's termux/ over)
set -e

HERE="$(cd "$(dirname "$0")" && pwd)"
mkdir -p "$HOME/.termux"

cp "$HERE/colors.properties"  "$HOME/.termux/colors.properties"
cp "$HERE/termux.properties"  "$HOME/.termux/termux.properties"

# Append the banner + prompt block once (idempotent).
MARKER="# --- NeverSoft Services CMD banner ---"
if ! grep -q "$MARKER" "$HOME/.bashrc" 2>/dev/null; then
  cat "$HERE/bashrc" >> "$HOME/.bashrc"
fi

# Silence Termux's own welcome message.
touch "$HOME/.hushlogin"

# Optional: drop CascadiaMono.ttf at ~/.termux/font.ttf for the exact CMD font.
if [ -f "$HERE/font.ttf" ]; then
  cp "$HERE/font.ttf" "$HOME/.termux/font.ttf"
fi

termux-reload-settings || true
echo "NeverSoft Command Prompt installed. Restart Termux (or open a new session)."
