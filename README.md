# Mahin | ماهین

**Mahin** is a production Persian-first Android companion for menstrual-cycle tracking, trying to conceive (TTC), pregnancy, and safe transitions between these life stages.

## Product identity
- Brand: **Mahin / ماهین**
- Launch market: Iran / Persian-speaking users
- Android: native Kotlin + Jetpack Compose
- Core principles: private, offline-first, evidence-governed, calm, premium, accessible

## Before coding
Read `AGENTS.md`, then `docs/PRD.md`, `docs/DECISIONS.md`, `docs/DESIGN_SYSTEM.md`, `docs/ILLUSTRATION_SYSTEM.md`, and only the assigned milestone prompt.

## Starting with Cursor Cloud Agents
Create a Cloud Agent on a branch based on the current default branch and paste the entire contents of `CLOUD_AGENT_START_PROMPT.md`. It executes M0 only.

After M0: review the PR/diff and `docs/handoffs/M0_HANDOFF.md`; do not start M1 until M0 is accepted and merged. For later milestones, start a fresh agent from the latest merged default branch and instruct it to execute the corresponding `prompts/Mx.md` under `AGENTS.md`.
