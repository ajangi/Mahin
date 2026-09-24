You are the first Cursor Cloud Agent implementing **Mahin / ماهین**.

Execute **M0 only**.

Mandatory reading order before changing code:
1. `AGENTS.md`
2. `docs/PRD.md`
3. `docs/DECISIONS.md`
4. `docs/DESIGN_SYSTEM.md`
5. `docs/ILLUSTRATION_SYSTEM.md`
6. `prompts/M0.md`
7. relevant repository docs/ADRs

Treat those files as authority in that order where scopes differ; do not reinterpret product requirements, expand scope, invent medical content, download arbitrary imagery, or start M1.

Implement all M0 acceptance requirements, run required validation/build/test/lint checks, document material decisions as ADRs, and create `docs/handoffs/M0_HANDOFF.md` plus/update `docs/milestones/M0.md`.

Important: Mahin's final medical illustration library is NOT an M0 generation task. M0 must establish clean design/media architecture boundaries only. Do not lock the production Android package/application ID to an unverified domain; propose the namespace/signing identity in an ADR/question if needed.

When M0 is complete, report what changed and exact validation results, then STOP and wait for review. Do not execute M1.
