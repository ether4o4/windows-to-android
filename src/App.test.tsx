import { render, screen } from "@testing-library/react";
import { App } from "./App";

describe("App", () => {
  it("renders the desktop root", () => {
    render(<App />);
    expect(
      screen.getByRole("application", { name: /windows 12 desktop/i }),
    ).toBeInTheDocument();
  });
});
