export const LIGHT_TOKENS = {
  "brand.primary": "#6E355D",
  "brand.primaryPressed": "#512544",
  "brand.primarySoft": "#C9A7BC",
  "surface.background": "#FCF9F7",
  "surface.default": "#FFFFFF",
  "surface.secondary": "#F4EFED",
  "text.primary": "#252126",
  "text.secondary": "#716970",
  "health.period": "#C94F62",
  "health.fertility": "#3B8F91",
  "health.ovulation": "#277276",
  "health.pregnancy": "#E99A73",
  "status.positive": "#47856A",
  "status.warning": "#D8913D",
  "status.critical": "#B83A45",
} as const;

export function cssVarForToken(token: keyof typeof LIGHT_TOKENS): string {
  return `--${token.replaceAll(".", "-")}`;
}
