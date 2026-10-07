# ADR 0022: M14c app shell and Settings

## Status
Proposed (pending owner approval)

## Context
M14a defined shell and Settings screen specs; M14b delivered `nav/*` icons. Today still hosted settings rows and the reproductive-mode card, and bottom navigation exposed six tabs in some modes with History as a tab.

## Decision
- Implement PRD §5 five-tab bottom navigation per `ReproductiveMode`, using `MahinIcons.Nav` and a custom `MahinBottomNavigationBar` (selected pill + restrained mode accent).
- Add a shell top app bar with a Settings entry on every top-level destination; History and Settings are secondary routes without the bottom bar.
- Move settings rows, export entry, and the reproductive-mode card to a dedicated Settings screen; relocate pregnancy appointments to the Plan tab without behaviour change.
- Keep feature-flag gating for Health Connect and assistant groups unchanged.

## Consequences
- Today and pregnancy hub layouts shrink; goldens for Today, pregnancy hub, and new shell/Settings baselines must be re-recorded where content changed.
- Post-pregnancy resume actions remain reachable from Settings › tracking goal when the pregnancy hub tab is unavailable.
