import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";
import { LIGHT_TOKENS, cssVarForToken } from "./tokens";

describe("admin design tokens", () => {
  it("match canonical design/tokens.json", () => {
    const raw = readFileSync(resolve(process.cwd(), "../design/tokens.json"), "utf8");
    const json = JSON.parse(raw) as { light: Record<string, string> };
    for (const [key, value] of Object.entries(LIGHT_TOKENS)) {
      expect(json.light[key].toLowerCase()).toBe(value.toLowerCase());
    }
  });

  it("maps brand.primary to CSS custom property", () => {
    expect(cssVarForToken("brand.primary")).toBe("--brand-primary");
  });
});
