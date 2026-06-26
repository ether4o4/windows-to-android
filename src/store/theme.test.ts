import { beforeEach, describe, expect, it } from "vitest";
import { applyTheme, resolveMode, useTheme } from "./theme";

describe("theme store", () => {
  beforeEach(() => {
    localStorage.clear();
    useTheme.setState({ mode: "dark", accent: "#4cc2ff" });
  });

  it("toggles between dark and light", () => {
    useTheme.getState().setMode("dark");
    expect(useTheme.getState().mode).toBe("dark");
    useTheme.getState().toggle();
    expect(useTheme.getState().mode).toBe("light");
    useTheme.getState().toggle();
    expect(useTheme.getState().mode).toBe("dark");
  });

  it("updates the accent colour", () => {
    useTheme.getState().setAccent("#9d5cff");
    expect(useTheme.getState().accent).toBe("#9d5cff");
  });

  it("resolves system mode to a concrete theme", () => {
    expect(["light", "dark"]).toContain(resolveMode("system"));
    expect(resolveMode("light")).toBe("light");
    expect(resolveMode("dark")).toBe("dark");
  });

  it("applies the theme to the document element", () => {
    applyTheme({ mode: "light", accent: "#ff5c8a" });
    expect(document.documentElement.getAttribute("data-theme")).toBe("light");
    expect(document.documentElement.style.getPropertyValue("--w-accent")).toBe(
      "#ff5c8a",
    );
  });

  it("persists changes to localStorage", () => {
    useTheme.getState().setAccent("#3fd07a");
    expect(localStorage.getItem("win12:theme")).toContain("#3fd07a");
  });
});
