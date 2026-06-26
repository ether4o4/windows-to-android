import { create } from "zustand";
import { loadJSON, saveJSON } from "../lib/persist";

export type ThemeMode = "light" | "dark" | "system";

export interface ThemeState {
  mode: ThemeMode;
  accent: string;
  /** CSS background value for the desktop wallpaper. */
  wallpaper: string;
  setMode: (mode: ThemeMode) => void;
  toggle: () => void;
  setAccent: (accent: string) => void;
  setWallpaper: (wallpaper: string) => void;
}

export const ACCENTS = [
  "#4cc2ff",
  "#0a84ff",
  "#9d5cff",
  "#ff5c8a",
  "#ff8d28",
  "#3fd07a",
  "#ffd60a",
  "#ff453a",
] as const;

export const WALLPAPERS = [
  // Default Windows-style "bloom" gradient.
  "radial-gradient(1200px 800px at 30% 20%, #1b6ec2 0%, #103a78 45%, #0a1730 100%)",
  "radial-gradient(1200px 800px at 70% 30%, #5b2a86 0%, #2a1450 50%, #0b0717 100%)",
  "radial-gradient(1200px 800px at 50% 0%, #0f7a6b 0%, #0a3d4d 50%, #061826 100%)",
  "linear-gradient(135deg, #ff7e5f 0%, #b14a8e 50%, #3b2667 100%)",
  "#101319",
] as const;

interface Persisted {
  mode: ThemeMode;
  accent: string;
  wallpaper: string;
}

const DEFAULTS: Persisted = {
  mode: "dark",
  accent: ACCENTS[0],
  wallpaper: WALLPAPERS[0],
};

function load(): Persisted {
  const p = loadJSON<Persisted>("theme", DEFAULTS);
  return { ...DEFAULTS, ...p };
}

/** Resolve "system" to a concrete theme using the OS / browser preference. */
export function resolveMode(mode: ThemeMode): "light" | "dark" {
  if (mode !== "system") return mode;
  const prefersDark =
    typeof window !== "undefined" &&
    typeof window.matchMedia === "function" &&
    window.matchMedia("(prefers-color-scheme: dark)").matches;
  return prefersDark ? "dark" : "light";
}

/** Push the current theme onto the document so CSS tokens take effect. */
export function applyTheme(state: Pick<ThemeState, "mode" | "accent">): void {
  if (typeof document === "undefined") return;
  const root = document.documentElement;
  root.setAttribute("data-theme", resolveMode(state.mode));
  root.style.setProperty("--w-accent", state.accent);
}

export const useTheme = create<ThemeState>((set, get) => {
  const initial = load();

  const persist = () => {
    const { mode, accent, wallpaper } = get();
    saveJSON("theme", { mode, accent, wallpaper });
    applyTheme({ mode, accent });
  };

  return {
    ...initial,
    setMode: (mode) => {
      set({ mode });
      persist();
    },
    toggle: () => {
      const next = resolveMode(get().mode) === "dark" ? "light" : "dark";
      set({ mode: next });
      persist();
    },
    setAccent: (accent) => {
      set({ accent });
      persist();
    },
    setWallpaper: (wallpaper) => {
      set({ wallpaper });
      persist();
    },
  };
});

// Apply the persisted theme immediately on module load (browser only).
applyTheme(load());
