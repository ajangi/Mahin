# Product Decisions — Frozen Baseline

These decisions are approved unless explicitly changed by the product owner.

- Market: Iran / Persian-speaking users only at launch.
- Product modes: Cycle tracking, Trying to Conceive (TTC), Pregnancy, plus safe transition states described in PRD.
- Pregnancy scope: full companion, not a lightweight countdown.
- Cycle/TTC logging: full scope in PRD.
- Educational/medical content: included; must be evidence-governed, sourced, reviewable, versioned, and publishable through CMS workflows.
- AI: architecture-ready only; no default launch chatbot. AI must never become the source of truth for medical content or core calculations.
- Identity: guest/local-first; account and cloud sync optional.
- Privacy: major product principle; discreet notifications, local protection, deletion/export, minimal telemetry, strict backend/admin access.
- Monetization: freemium; core tracking and essential safety information cannot be paywalled.
- Android: native Kotlin + Jetpack Compose.
- Backend/admin: production backend, sync, CMS/admin, operations are in scope.
- Visual direction: modern, premium, calm, feminine without stereotypical pink; Persian-first RTL.
- Calendar UX: Jalali-first for Iranian users, Gregorian interoperability/canonical storage.

## Brand & visual system — approved
- Product name: **Mahin / ماهین**.
- Primary brand color: Mahin Mulberry `#6E355D`; warm ivory background `#FCF9F7`.
- Semantic anchors: recorded period `#C94F62`, fertility `#3B8F91`, ovulation `#277276`, pregnancy `#E99A73`.
- Detailed tokens and visual rules are authoritative in `docs/DESIGN_SYSTEM.md`.
- Illustration/media is a first-class governed system; see `docs/ILLUSTRATION_SYSTEM.md`.
- Medically meaningful illustrations require source/provenance, versioning and review/approval; agents may not invent them as medical truth.
- Content media architecture: CMS/admin -> object storage -> CDN -> Android caching/offline degradation. Core UI assets may remain bundled.
- Do not lock an Android application ID/package namespace to an unverified domain in M0. Propose it in an ADR and obtain owner approval before a production signing/store identity becomes costly to change.
