import { useEffect } from "react";
import { applyTheme, useTheme } from "./store/theme";

export function App() {
  const wallpaper = useTheme((s) => s.wallpaper);
  const mode = useTheme((s) => s.mode);
  const accent = useTheme((s) => s.accent);

  // Keep the document theme in sync with the store.
  useEffect(() => {
    applyTheme({ mode, accent });
  }, [mode, accent]);

  return (
    <div
      className="desktop"
      role="application"
      aria-label="Windows 12 desktop"
      style={{ background: wallpaper }}
    >
      <div className="desktop__boot">Windows 12</div>
    </div>
  );
}
