# Production Product Requirements Document
## Persian Period, Fertility & Pregnancy Companion — Android

**Status:** Production specification v1.0  
**Target market:** Iran / Persian-speaking users  
**Primary client:** Native Android (Kotlin + Jetpack Compose)  
**Product stage:** Production launch, not MVP  
**Primary language:** Persian (fa-IR), RTL  
**Calendars:** Jalali-first UX with Gregorian interoperability  

---

## 1. Product Vision

Build the most trustworthy, private, polished Persian-first reproductive-health companion for Android: one application that supports users across menstrual-cycle tracking, trying to conceive, pregnancy, and the transition between these life stages.

The product should feel calm, premium, modern and human rather than clinical or stereotypically feminine. It must be useful without requiring an account, work reliably with poor or absent connectivity, handle highly sensitive reproductive-health data conservatively, and clearly separate estimates/education from medical diagnosis.

The app is not a medical device and must not diagnose, prescribe, replace a clinician, or present cycle/fertility estimates as contraception.

### 1.1 Product principles

1. **Privacy by default.** Intimate data is collected only when needed and never used for advertising targeting.
2. **Useful before signup.** A user can begin tracking locally without registration.
3. **Persian-native, not translated.** RTL layout, Persian copy, Jalali dates, culturally appropriate UX and local terminology are first-class.
4. **Evidence-aware.** Medical/educational content has sources, clinical review status, review dates and versions.
5. **Predictions are estimates.** Confidence and uncertainty are visible; predictions are never framed as certainties.
6. **Continuity.** Cycle, TTC and pregnancy are modes in one longitudinal health timeline.
7. **Core tracking is free.** Subscription enhances the experience rather than holding essential health tracking or safety information hostage.
8. **Offline first.** Logging, calendar history and downloaded/current relevant content remain usable without network access.
9. **Accessible and discreet.** Biometric/PIN lock, privacy-conscious notifications, accessible UI and neutral app-surface options.
10. **Production operability.** Monitoring, auditability, backups, content publishing controls and migration paths are requirements, not post-launch extras.

---

## 2. Success Criteria

### 2.1 North-star behavior

Users consistently log relevant reproductive-health data and return because the application turns that history into understandable, trustworthy context.

### 2.2 Launch KPIs

Instrument these without collecting unnecessary sensitive payloads:

- onboarding completion rate
- first period/pregnancy setup completion
- D1 / D7 / D30 retention
- weekly active trackers
- percentage of users with >= 2 completed cycles
- logging frequency by category (category only; avoid raw intimate values in analytics)
- reminder opt-in rate
- account conversion from guest
- cloud backup/sync adoption
- pregnancy weekly-content engagement
- content save/share rate
- free-to-premium conversion
- subscription renewal/cancellation
- crash-free users and sessions
- ANR rate
- sync failure rate
- notification delivery/open rate
- support issue categories

No analytics event may contain free-text notes, sexual-activity details, symptoms, pregnancy test results, precise cycle dates, or other raw reproductive-health data.

---

## 3. Personas & Jobs To Be Done

### Persona A — Cycle tracker
Wants to understand when periods may occur, remember symptoms and see patterns over time.

### Persona B — TTC user
Wants cycle context, fertile-window estimates and a structured place to record BBT, cervical mucus, ovulation tests, intercourse and pregnancy tests without being promised conception.

### Persona C — Pregnant user
Wants a week-by-week companion, gestational-age context, appointments/tests/checklists, symptoms, weight, fetal movement and contractions in one place.

### Persona D — Privacy-first user
Wants useful tracking without creating an account and wants the application to reveal as little as possible on lock screens and notifications.

### Persona E — Irregular-cycle user
Needs history and trends without misleading high-confidence predictions.

---

## 4. Product Modes & State Machine

Top-level reproductive state:

- `CYCLE_TRACKING`
- `TRYING_TO_CONCEIVE`
- `PREGNANT`
- `POST_PREGNANCY_TRANSITION`
- `TRACKING_PAUSED`

Mode changes are explicit user actions. Never infer pregnancy solely from a late period or logged symptoms.

### 4.1 Mode transition rules

**Cycle -> TTC:** preserve all historical cycles; reveal fertility-focused logging and insights.

**Cycle/TTC -> Pregnant:** require user confirmation. Setup asks for dating source: LMP, clinician/ultrasound EDD, conception/ovulation date if known. A clinician/ultrasound-confirmed EDD can be designated authoritative.

**Pregnant -> Post-pregnancy:** user may record birth, pregnancy loss, termination, or choose “pregnancy ended / prefer not to specify.” Language must be compassionate and neutral. Do not automatically switch to celebratory postpartum content without confirmation of outcome.

**Post-pregnancy -> Cycle/TTC:** user chooses when to resume tracking. Predictions must account for insufficient recent cycle data and should not assume regularity.

Historical data is never destroyed by a mode switch.

---

## 5. Information Architecture

Primary bottom navigation changes contextually but retains stable mental models.

### Cycle / TTC

1. **Today — امروز**
2. **Calendar — تقویم**
3. **Log — ثبت**
4. **Insights — تحلیل‌ها**
5. **Learn — دانستنی‌ها**

Profile/settings are available from the top app bar.

### Pregnancy

1. **Today — امروز**
2. **Pregnancy — بارداری**
3. **Log — ثبت**
4. **Plan — برنامه**
5. **Learn — دانستنی‌ها**

---

## 6. Onboarding

### 6.1 Entry

- splash
- privacy-first value proposition
- choose current goal:
  - پیگیری پریود و چرخه
  - قصد بارداری دارم
  - باردار هستم
- explain that account creation is optional
- privacy summary with link to full policy
- notification choice later in context; do not demand notification permission immediately

### 6.2 Cycle/TTC setup

Collect only what is needed:

- last period start date
- optional end date
- typical cycle length, if known
- typical period length, if known
- regular / somewhat irregular / irregular / not sure
- optional prior period dates to improve estimates
- birth year/age range only if required for product behavior; avoid full DOB unless justified
- goals/preferences

If history is insufficient, display low-confidence estimates and explain why.

### 6.3 Pregnancy setup

Support:

- first day of LMP
- known estimated due date
- clinician/ultrasound-confirmed EDD
- known conception/ovulation date (secondary estimate path)
- singleton/multiple if user knows; “not sure” supported

Store both original inputs and derived dates. Never silently overwrite an EDD. Maintain `dating_method`, `effective_edd`, and audit/history when the user changes it.

### 6.4 Guest-first identity

Generate a local installation/user identity. Explain:

- data is stored on this device
- optional account enables backup and multi-device recovery
- deleting the app may remove unsynced local data

Offer account creation after the user receives initial value, and from Settings/Backup.

---

## 7. Cycle Tracking Requirements

### 7.1 Calendar

Jalali calendar is the primary display for Iranian users. Gregorian dates remain available in details/export and are canonical internally.

Calendar states:

- logged period days
- predicted period range
- estimated fertile window
- estimated ovulation day/range
- today
- logged events
- pregnancy state where relevant

Use distinguishable shapes/patterns in addition to color for accessibility.

### 7.2 Period logging

Fields:

- start/end
- flow: spotting / light / medium / heavy / very heavy
- optional clots flag with neutral wording
- notes

Allow correcting past cycles, merging accidental duplicate periods, and splitting incorrectly logged ranges.

### 7.3 Daily log categories

**Symptoms:** cramps, headache, migraine, bloating, breast tenderness, acne, back pain, nausea, fatigue, digestive changes, cravings, insomnia, dizziness, other configurable symptoms.

**Mood:** calm, happy, energetic, sensitive, irritable, anxious, sad/low, stressed, mood swings, custom note.

**Pain:** location, severity 0–10, optional note.

**Discharge/cervical mucus:** dry, sticky, creamy, watery, egg-white/slippery, unusual/other. Educational copy must avoid diagnosing infection.

**Sexual activity:** optional, explicit opt-in category; protected/unprotected where the user chooses; libido optional. This data must never appear in analytics payloads or notification text.

**Medication/supplements:** name, amount as free text, time, optional reminder. Do not recommend doses from user-entered medication records.

**Measurements:** weight, BBT, optional resting heart rate if supported later.

**Tests:** ovulation test negative/positive/peak/unclear; pregnancy test negative/positive/unclear.

**Lifestyle:** sleep duration/quality, exercise, water and stress may be offered as optional modules, disabled by default to prevent logging overload.

**Notes:** encrypted free text; never indexed into third-party analytics/search.

### 7.4 Prediction engine

Separate deterministic/domain logic from UI and backend.

Inputs may include:

- completed cycle history
- recent cycle lengths
- period lengths
- user-declared regularity
- BBT and ovulation-test signals where available
- user-confirmed ovulation data

Outputs:

- next-period estimate/range
- cycle-day number
- estimated fertile window
- estimated ovulation day/range
- confidence level / insufficient-data state

Requirements:

- prediction versions are stored (`algorithm_version`)
- predictions can be recomputed without rewriting historical logged facts
- irregular cycles widen uncertainty rather than inventing precision
- do not claim a “safe day”
- fertility estimates must include clear non-contraception language
- do not infer pregnancy from lateness
- all calculations have unit tests against documented fixtures and edge cases

---

## 8. Trying-to-Conceive Mode

TTC adds, rather than replaces, cycle functionality.

### Features

- fertile-window visualization with uncertainty
- BBT chart
- ovulation-test timeline
- cervical-mucus timeline
- intercourse logging
- pregnancy-test logging
- cycle comparison
- configurable reminders
- educational TTC content
- “when to consider speaking with a clinician” content only from reviewed sources; no personalized diagnosis

Do not create gamified pressure (“you missed your chance”) or guarantee conception probability.

---

## 9. Pregnancy Module

### 9.1 Pregnancy home

Show:

- gestational age in weeks + days
- current trimester
- EDD countdown
- current week card
- fetal-development summary
- maternal/body-change summary
- relevant checklist items
- upcoming user-entered appointments/tests
- recent logs
- quick actions

Avoid fruit-size comparisons as the only representation; if used, label them playful approximations rather than medical measurements.

### 9.2 Dating

Maintain:

- LMP
- calculated LMP-based EDD
- clinician/ultrasound EDD when entered
- effective EDD
- dating source
- date changed and optional reason

Conventional LMP calculation uses 280 days as an estimate; UX must explain assumptions and allow clinician-confirmed dating to supersede the display estimate.

### 9.3 Week-by-week content

For every pregnancy week, CMS supports:

- fetal development
- maternal changes
- common experiences/symptoms
- nutrition/lifestyle education
- appointment/test context
- preparation/checklist suggestions
- warning-sign content
- references
- reviewer and review date
- content version

Content is not hard-coded into the Android binary.

### 9.4 Pregnancy logs

- symptoms
- weight
- blood pressure (manual log; no interpretation beyond reviewed educational thresholds/workflows)
- medications/supplements
- notes
- appointments
- tests/labs as user-entered records/files in later phase
- fetal movement/kicks
- contractions
- sleep/mood optionally

### 9.5 Kick counter

- explicit start/stop session
- tap to record movement
- session duration/count/history
- configurable personal reminder
- reviewed educational instructions
- never reassure solely based on app count; warning copy directs users to professional care when concerned

### 9.6 Contraction timer

- start/stop contraction
- duration
- interval
- session history
- clear emergency/safety messaging authored/reviewed medically
- no autonomous diagnosis of labor

### 9.7 Appointments & tests

User can create:

- clinician/midwife visit
- ultrasound
- laboratory test
- screening
- vaccination
- custom

Fields: title, Jalali/Gregorian-compatible datetime, location, clinician, notes, reminder.

Recommended timelines live in CMS/config and are educational—not a substitute for the user’s care plan.

### 9.8 Pregnancy outcome

Support sensitively:

- birth
- pregnancy loss
- termination
- ended / prefer not to specify

After loss/termination, suppress celebratory pregnancy notifications immediately. Provide a minimal choice about whether the user wants related educational/support content; never force it.

---

## 10. Educational Content & Medical Governance

### 10.1 Source hierarchy

Editorial/clinical team should prioritize current guidance from authoritative organizations and applicable Iranian clinical/public-health guidance where available. International references may include WHO and recognized obstetric/gynecologic professional bodies. Localization must be reviewed for Iranian clinical context rather than mechanically translated.

### 10.2 Content object

Every medically relevant content record includes:

- `id`
- `slug`
- `locale`
- `title`
- `summary`
- `body_richtext`
- `content_type`
- `life_stage`
- `gestational_week` if applicable
- `tags`
- `medical_risk_level`
- `source_references[]`
- `source_publication_date`
- `source_last_checked_at`
- `clinical_reviewer`
- `clinical_reviewed_at`
- `next_review_due_at`
- `content_version`
- `status: draft/review/approved/published/retired`
- `effective_from`
- `effective_to`

### 10.3 Publishing workflow

`DRAFT -> MEDICAL_REVIEW -> EDITORIAL_REVIEW -> APPROVED -> SCHEDULED/PUBLISHED -> RETIRED`

High-risk warning content cannot be published without medical approval. Every edit creates a version. Emergency copy requires four-eyes approval.

### 10.4 Content freshness

Admin dashboard flags overdue clinical review and changed/dead references. Published content can be withdrawn remotely without app release.

### 10.5 Safety presentation

Distinguish visually:

- general education
- “contact your clinician” guidance
- urgent/emergency guidance

The app must not produce individualized diagnosis from symptoms.

---

## 11. Search & Learn

Persian search supports normalized Arabic/Persian characters (`ي/ی`, `ك/ک`), half-spaces and common spelling variants.

Features:

- browse by Cycle / Fertility / Pregnancy / Symptoms / Nutrition / Tests / Lifestyle
- pregnancy-week contextual feed
- save/bookmark
- recently viewed locally
- source/review information accessible from article details

Do not index private user notes in content search.

---

## 12. Notifications & Reminders

### Categories

- predicted upcoming period
- period logging follow-up
- medication reminder (user-created)
- TTC logging reminders
- pregnancy weekly update
- appointment/test reminder
- kick-counter personal reminder
- custom reminder

### Privacy modes

Default notification wording should be discreet, e.g. “یادآوری شما آماده است” rather than exposing pregnancy, menstruation, sexual activity or medication on a lock screen.

User can choose:

- discreet content
- descriptive content
- no notifications

Each category is independently configurable. Request Android notification permission contextually, only after user enables a reminder/value requiring it.

---

## 13. Insights & Reports

### Free

- cycle history
- average/range of cycle length
- average/range of period length
- recent symptoms timeline
- basic trends
- pregnancy logs/history
- essential pregnancy weekly information

### Premium candidates

- advanced multi-cycle trend exploration
- configurable correlations presented as observations, not causation
- richer charts and comparisons
- extended export/report layouts
- advanced personalization
- premium content collections
- future AI assistant
- optional family/partner sharing only if a safe product design is later approved

Never paywall emergency/safety information, basic period logging, basic predictions, pregnancy dating, essential pregnancy tracking, account deletion, data export or privacy controls.

---

## 14. Monetization

Use a server-driven entitlement model:

- `FREE`
- `PREMIUM_MONTHLY`
- `PREMIUM_ANNUAL`
- promotional/trial entitlements

Do not hard-code price strings. Backend stores product mapping and feature flags; Google Play Billing is authoritative for Play-distributed builds. Architecture should permit an Iran-appropriate distribution/billing adapter if alternative stores/channels are later required, without contaminating domain logic.

Subscription cancellation never deletes health data.

---

## 15. Privacy & Security

Reproductive-health data is treated as highly sensitive.

### 15.1 Data minimization

- no account required for core use
- no advertising identifier requirement
- no sale of health data
- no targeted advertising based on health/reproductive data
- third-party SDKs minimized and security-reviewed

### 15.2 Device security

- Android Keystore-backed key material
- encrypted sensitive local storage/database
- biometric/PIN app lock
- configurable lock timeout
- hide sensitive content in recent-app snapshots when privacy lock enabled
- optional screenshot blocking for sensitive screens; evaluate UX trade-off
- redact sensitive logs

### 15.3 Backend security

- TLS everywhere
- encryption at rest
- password hashing with modern adaptive algorithm if passwords are used
- short-lived access tokens + rotating refresh-token strategy
- session/device management
- rate limiting
- brute-force protections
- least-privilege IAM
- secrets manager; no secrets in repository/images
- separate prod/staging credentials
- audited admin actions
- database backups encrypted and restore-tested
- object storage private by default with short-lived signed URLs
- vulnerability/dependency scanning

### 15.4 Account deletion

In-app flow must support permanent account deletion. Define deletion queue, grace period if legally/product appropriate, backup-retention behavior, and auditable completion. Guest users can erase all local data immediately.

### 15.5 Export

Provide user-readable export (PDF/CSV/JSON as appropriate) without requiring Premium. Export requires re-authentication/app unlock when privacy lock is active.

---

## 16. Authentication & Sync

### 16.1 Identity

Architecture supports:

- guest/local identity
- phone/email account adapter as product chooses for Iran
- future additional identity providers

Do not make Google identity mandatory.

### 16.2 Guest conversion

Guest -> registered migration is transactional and preserves local IDs through mapping. It must not duplicate cycles/logs.

### 16.3 Offline-first sync

Local Room database is the UI source of truth. Writes occur locally first and enter an outbox. Sync worker sends mutations when network becomes available.

Each syncable entity has:

- UUID generated client-side
- `created_at`
- `updated_at`
- `deleted_at` tombstone where needed
- server revision/version
- last synced revision

Conflict strategy is entity-specific. Simple logs can use field-aware last-write rules; destructive/structural conflicts require deterministic merge policies. Never silently discard health records.

Use idempotency keys for mutation retries.

---

## 17. Android Architecture

### 17.1 Stack

- Kotlin
- Jetpack Compose + Material 3
- single-activity architecture
- Navigation Compose
- ViewModel + StateFlow
- coroutines
- Room
- DataStore
- WorkManager
- Hilt
- Retrofit/OkHttp or equivalent typed HTTP layer
- Kotlin serialization
- Android Keystore/security primitives
- Google Play Billing abstraction
- Firebase Cloud Messaging or replaceable push provider abstraction
- Crash/observability provider behind privacy-reviewed interface

Pin actual dependency versions in a version catalog at implementation time; do not copy stale versions from this PRD.

### 17.2 Modularization

Suggested modules:

```text
:app
:core:designsystem
:core:model
:core:database
:core:network
:core:datastore
:core:security
:core:sync
:core:analytics
:core:notifications
:core:testing
:domain:cycle
:domain:fertility
:domain:pregnancy
:domain:content
:domain:account
:domain:subscription
:feature:onboarding
:feature:today
:feature:calendar
:feature:log
:feature:insights
:feature:ttc
:feature:pregnancy
:feature:kicks
:feature:contractions
:feature:appointments
:feature:learn
:feature:profile
:feature:settings
:feature:privacy
:feature:paywall
```

Avoid premature micro-modules; modules should enforce domain boundaries and build performance, not create ceremony.

### 17.3 Domain rules

Prediction/dating algorithms are pure Kotlin domain code with no Android dependencies. Date calculations use Gregorian canonical instants/local dates internally; Jalali is presentation/input conversion, preventing ambiguous persisted dates.

---

## 18. Backend Architecture

Recommended starting architecture: **modular monolith**, not microservices. Reproductive data consistency and team velocity are more valuable initially than distributed complexity.

### 18.1 Suggested stack

Implementation team may choose based on operational expertise. Reference production shape:

- REST API with OpenAPI contract
- PostgreSQL
- Redis for rate limiting/cache/jobs where justified
- S3-compatible object storage
- background job worker
- push-notification worker
- admin/CMS web application
- containerized deployment
- CDN for public content media
- managed secrets
- structured logging + metrics + tracing

### 18.2 Backend modules

- identity/auth
- users/devices/sessions
- reproductive profiles
- cycles/periods/logs
- fertility observations
- pregnancies/dating
- pregnancy logs
- appointments
- content/CMS
- notifications
- sync
- entitlements/billing
- exports
- privacy/deletion
- admin/audit
- feature flags/config

### 18.3 API principles

- versioned API (`/v1`)
- OpenAPI source of truth
- request validation
- idempotent mutation support
- cursor pagination
- stable machine error codes
- UTC timestamps; explicit local dates where medically/calendar relevant
- locale independent persistence
- optimistic concurrency where needed
- no PII/sensitive health payloads in access logs

---

## 19. Core Data Model

Key entities (not exhaustive):

```text
User
Device
Session
UserPreferences
PrivacySettings
ReproductiveProfile
Cycle
PeriodEpisode
DailyLog
SymptomLog
MoodLog
PainLog
FlowLog
CervicalMucusLog
SexualActivityLog
MedicationLog
MeasurementLog
BBTLog
OvulationTestLog
PregnancyTestLog
PredictionSnapshot
Pregnancy
PregnancyDatingRevision
PregnancySymptomLog
KickSession
KickEvent
ContractionSession
ContractionEvent
Appointment
Reminder
ContentArticle
ContentVersion
MedicalReference
ClinicalReview
Bookmark
Subscription
Entitlement
SyncMutation
ExportJob
DeletionRequest
AdminUser
AuditEvent
FeatureFlag
RemoteConfig
```

Separate high-sensitivity records logically and through access policy. Do not collapse every daily observation into an unvalidated JSON blob simply for implementation speed; typed records are required for correctness, migrations and future insights.

---

## 20. Jalali / Persian Localization

Requirements:

- full RTL composition and navigation awareness
- Persian typography optimized for Android
- Persian numerals configurable; ensure numeric input interoperability
- Jalali date picker built/tested as a first-class component
- Gregorian date shown optionally in detail contexts
- internal canonical date conversion tests around leap years/year boundaries
- correct pluralization/copy rather than English interpolation patterns
- normalize Persian/Arabic characters in search
- handle half-space correctly
- do not mirror icons whose meaning should remain directional/semantic incorrectly
- support font scaling and TalkBack

All user-visible strings are resources; no hard-coded Persian text in composables.

---

## 21. Design System

### Brand direction

Product identity: **Mahin / ماهین**. Modern, warm, confident, premium and calm. Avoid “everything pink,” babyish illustrations, excessive flowers/hearts, and overly clinical hospital aesthetics. The frozen palette, dark-mode baseline and token governance are defined in `docs/DESIGN_SYSTEM.md`. Illustration/media rules and medical-image governance are defined in `docs/ILLUSTRATION_SYSTEM.md`.

### Foundation

- Material 3 adapted into a custom design system
- light theme at launch; dark theme supported or scheduled before GA depending QA capacity
- accessible contrast
- 8dp spacing grid
- rounded but not toy-like surfaces
- clear typography hierarchy
- subtle motion
- charts designed for RTL labels and accessibility
- semantic colors for period/prediction/fertility/pregnancy states; never color-only meaning

### Key reusable components

- Persian/Jalali date picker
- cycle calendar cell
- prediction range indicator
- daily log sheet
- symptom selector
- severity control
- privacy-aware value card
- pregnancy week card
- timeline
- chart primitives
- appointment card
- evidence/source footer
- safety callout
- premium badge/paywall component
- empty/error/offline/sync states

Create screenshot tests for critical design-system components and major screens.

---

## 22. Accessibility

- TalkBack semantics for all interactive controls
- minimum touch targets
- dynamic font scaling
- no essential color-only communication
- chart textual summaries
- logical RTL focus order
- reduced-motion consideration
- content descriptions only where meaningful (avoid noisy duplication)
- screen-reader friendly date expressions

---

## 23. Health Connect

Design an optional integration boundary; do not make Health Connect mandatory for launch.

Potential supported records should be verified against the current Android Health Connect SDK/policy during implementation. Any permissions requested must map to an obvious user-facing feature. Explain why each permission is requested before triggering the system permission dialog.

Never request broad health permissions “for future use.”

---

## 24. AI-Ready Architecture (Not Launch Feature)

Create interfaces and data contracts now, but do not ship an unrestricted medical chatbot in V1.

Future assistant principles:

- retrieval from medically approved content corpus
- explicit provenance/citations in answers
- user grants granular permission before private tracker context is sent
- server-side redaction/minimization
- no diagnosis/prescription
- urgent-risk escalation rules are deterministic and clinically reviewed, not left solely to an LLM
- prompt/model/version logging without storing unnecessary intimate conversation content
- evaluation suite for Persian safety, hallucination, medical grounding and crisis/urgent scenarios
- ability to disable AI remotely

Suggested boundary:

```kotlin
interface HealthAssistantGateway {
    suspend fun ask(request: AssistantRequest): AssistantResponse
}
```

No LLM vendor-specific code in domain modules.

---

## 25. Analytics

Use a privacy-reviewed event taxonomy.

Allowed example:

```text
onboarding_started
onboarding_completed(mode)
calendar_opened
log_category_opened(category)
log_saved(category)
prediction_explanation_opened
pregnancy_week_opened(week)
appointment_created(type)
article_opened(article_id)
paywall_viewed(entry_point)
subscription_started(plan)
sync_failed(error_class)
```

Forbidden analytics properties include exact period dates, free-text notes, sexual activity values, pregnancy-test result, symptom detail tied to identity, medication names, pregnancy outcome, or exported health data.

Product analytics user ID should be pseudonymous and separable from medical record identifiers where practical.

---

## 26. Observability & Operations

Monitor:

- API latency/error rate
- auth failures/rate-limit anomalies
- job queues
- push failures
- sync errors/conflicts
- DB capacity/connections
- backup success
- object storage errors
- content publication errors
- Android crash/ANR rates
- billing acknowledgement/entitlement mismatches

Sensitive request/response bodies must never be captured by default tracing or error-reporting tools.

Create operational runbooks for auth outage, sync incident, accidental content publication, notification incident, billing incident, suspected breach, database restore and compromised admin account.

---

## 27. Admin / CMS

Roles:

- super admin
- medical reviewer
- editor
- support (restricted)
- analyst (aggregated/non-sensitive)

Capabilities:

- article CRUD/versioning
- source/reference management
- clinical approval workflow
- pregnancy-week content management
- warning/safety copy management with strict approval
- publish/schedule/unpublish
- feature flags
- remote config
- notification campaign templates (no private-health segmentation without explicit approved design)
- entitlement/support tools with least privilege
- audit trail
- content freshness dashboard

Support agents must not have default access to reproductive records. Any exceptional access workflow should require explicit authorization, reason and audit logging.

---

## 28. Feature Flags & Remote Configuration

Server-controlled flags for risky/new features. Examples:

- TTC advanced charts
- Health Connect
- premium insights
- AI assistant
- experimental onboarding

Remote config can control copy/configuration but must not silently alter clinically significant algorithms without an algorithm-version release/audit mechanism.

---

## 29. Error, Empty & Edge States

Explicitly design/test:

- first-ever period
- insufficient history
- cycles < expected range / very long cycles
- highly variable cycles
- missed logging for months
- corrected historical period
- overlapping accidental periods
- timezone change
- Persian/Gregorian conversion boundaries
- device clock change
- offline for weeks
- guest reinstall/data loss warning
- guest -> account migration
- two devices editing same day
- pregnancy EDD revision
- pregnancy begins mid-cycle
- pregnancy outcome before term
- multiple pregnancy if known
- post-term display
- subscription expires offline
- notification permission denied
- biometric unavailable/changed
- server maintenance
- content withdrawn while cached
- deletion while offline

No screen should terminate in a blank state or raw backend exception.

---

## 30. Testing Strategy

### Android

- unit tests: domain algorithms, dating, date conversion, validation
- property/parameterized tests for cycle/date calculations
- Room migration tests
- repository/sync tests
- ViewModel tests
- Compose UI tests
- screenshot/golden tests for critical RTL screens
- accessibility checks
- offline/poor-network tests
- process death/state restoration
- notification tests
- billing test flows
- security tests for lock/session behavior

### Backend

- unit tests
- integration tests with real PostgreSQL/Redis in CI
- API contract tests
- authorization matrix tests
- idempotency tests
- concurrency tests
- migration tests
- backup/restore drills
- deletion/export tests
- billing webhook replay/idempotency tests
- CMS publishing permission tests

### Clinical/content QA

- source verification
- reviewer approval
- pregnancy-week boundary checks
- red-flag copy verification
- stale-content review workflow

### Release gate

No production release if:

- critical/high security issue open
- destructive migration unverified
- core prediction/dating test suite failing
- crash/ANR thresholds unacceptable
- privacy declaration mismatches actual behavior
- high-risk medical content lacks required review

---

## 31. CI/CD & Environments

Environments:

- local
- CI/test
- staging
- production

Pipelines:

**Android PR:** lint -> formatting -> unit -> static analysis -> build -> UI/screenshot relevant tests.

**Backend PR:** lint -> unit -> integration -> contract -> migration validation -> security/dependency scan -> image build.

**Release:** signed artifacts, immutable versioning, changelog, DB migration plan, rollback/forward-fix plan, staged Android rollout.

Production secrets never enter CI logs. Require branch protection and review for production infrastructure/content safety changes.

---

## 32. Distribution & Compliance

Before every store release, verify current Google Play health-app, privacy, Data Safety, account-deletion, billing and target-SDK requirements rather than relying on this static document.

Maintain a compliance matrix mapping:

`declared data -> actual collection -> storage -> purpose -> retention -> sharing -> deletion behavior -> store disclosure`

Health permissions and any Health Connect integration must be requested only for implemented, user-visible features.

Because the initial market is Iran, product/legal review must also cover applicable local distribution, payment, health-content, privacy and infrastructure constraints before launch.

---

## 33. Medical Baseline Used by Product Design

This PRD intentionally does not embed a complete medical corpus. The CMS must be populated through a separate clinically reviewed content project.

Baseline principles validated for architecture:

- Conventional estimated due date from LMP is 280 days/40 weeks, but LMP assumptions can be inaccurate and early ultrasound/clinical dating can improve dating accuracy; the product therefore stores dating source and supports clinician-confirmed EDD.
- Antenatal care is ongoing professional care, not an app workflow. WHO guidance recommends a minimum of eight antenatal contacts in its ANC model; the app may educate/remind but does not replace the user's clinician-specific schedule.

Reference starting points:

- American College of Obstetricians and Gynecologists (ACOG), *Methods for Estimating the Due Date*.
- World Health Organization (WHO), *Recommendations on antenatal care for a positive pregnancy experience* and subsequent updates.

At implementation/content-population time, verify the current versions and applicable Iranian guidance.

---

## 34. Definition of Production Ready

The app is **not production-ready** merely because screens compile. GA requires:

- complete core flows for all three modes
- tested prediction and pregnancy dating domain logic
- local/offline functionality
- optional account + robust sync
- privacy lock and deletion/export
- reviewed educational content
- production CMS/admin
- production notification system
- entitlement/billing implementation
- observability and runbooks
- backups and restore test
- security review
- accessibility pass
- RTL/Jalali QA
- store compliance review
- crash/ANR quality gate
- staging soak test
- documented incident response

---

# 35. Cursor Implementation Plan

Cursor agents should implement this product through milestones. **Do not ask one agent to build the entire application in one pass.** Each milestone must end with tests, documentation and a clean handoff.

## M0 — Repository & Engineering Foundation

Deliverables:

- monorepo or clearly coordinated Android/backend/admin repositories
- Android multi-module skeleton
- backend modular-monolith skeleton
- admin skeleton
- Docker local dependencies
- CI
- environment configuration
- lint/format/static analysis
- test harnesses
- OpenAPI setup
- ADR folder
- threat-model skeleton
- `README.md`, `CONTRIBUTING.md`, `ARCHITECTURE.md`
- dependency/version catalog
- no placeholder architecture that must be rewritten in M1

Exit: all applications build, tests execute, local environment is reproducible.

## M1 — Design System, RTL & Calendar Foundation

- design tokens/components
- typography
- navigation shell
- RTL correctness
- Jalali/Gregorian conversion library boundary
- Persian/Jalali date picker
- accessibility foundations
- screenshot tests
- empty/loading/error components

Exit: representative screens demonstrate production-quality RTL UI and date handling.

## M2 — Local-First Cycle Tracking

- onboarding for Cycle/TTC
- Room schema
- period logging/editing
- daily logs
- calendar
- Today
- history
- deterministic prediction engine v1
- confidence/insufficient-data states
- domain tests/fixtures

Exit: complete useful cycle tracker works with airplane mode and no account.

## M3 — TTC

- TTC transition
- BBT
- OPK
- cervical mucus
- intercourse/pregnancy-test logging
- TTC timeline/charts
- fertility estimate UX and safety language

Exit: TTC is production-complete locally.

## M4 — Pregnancy

- pregnancy onboarding and dating
- mode transition
- pregnancy Today/week experience
- symptoms/weight/BP logs
- appointments/tests
- kick counter
- contraction timer
- outcome flow
- sensitive outcome notification suppression

Exit: complete local pregnancy companion with tests.

## M5 — Backend Identity & Sync

- guest identity mapping
- account creation/auth
- API implementation
- server schema
- outbox sync
- conflict handling
- session/device management
- guest conversion
- deletion/export foundations
- backend security baseline

Exit: offline-first multi-session synchronization passes deterministic integration scenarios without data loss/duplication.

## M6 — CMS & Evidence-Governed Content

- CMS roles
- content model/versioning
- source references
- medical/editorial workflow
- pregnancy weekly content API
- Learn/search/bookmarks
- remote withdrawal/cache invalidation
- freshness dashboard

Exit: approved content can be published/withdrawn without Android release and has full audit history.

## M7 — Notifications

- reminder domain
- WorkManager local reminders
- push infrastructure where needed
- privacy/discreet modes
- permission UX
- appointment/weekly/cycle reminders
- timezone handling

Exit: reminders are reliable, privacy-safe and independently configurable.

## M8 — Insights, Export & Premium

- free insights
- advanced premium insights
- entitlement system
- billing adapter + Play Billing implementation
- paywall
- restore purchases
- subscription state handling
- user export

Exit: entitlement behavior is server-verifiable and core tracking remains functional after Premium expiry.

## M9 — Privacy/Security Hardening

- biometric/PIN lock
- local encryption validation
- sensitive screen/log protections
- admin least privilege
- audit logs
- rate limiting
- security headers/config
- deletion workflow end-to-end
- threat-model review
- dependency/security scans
- backup restore exercise

Exit: security checklist and high-risk findings closed.

## M10 — Health Connect (Optional Launch Flag)

- current SDK/policy verification
- permission education
- minimal record permissions
- import/export mapping only where product-approved
- revocation behavior
- tests

Exit: feature can be remotely disabled without affecting core tracker.

## M11 — Production Hardening & Release

- performance profiling
- accessibility audit
- Persian linguistic QA
- RTL/Jalali edge QA
- crash/ANR review
- sync chaos scenarios
- backend load tests
- staging soak
- store assets/config
- privacy/Data Safety verification
- operational dashboards/runbooks
- release checklist
- staged rollout configuration

Exit: GA release candidate.

## M12 — AI Foundation / Future (No default GA chatbot)

- approved-content retrieval pipeline
- assistant gateway
- consent model
- safety/evaluation harness
- citations/provenance
- deterministic escalation layer
- provider abstraction
- remote kill switch

Exit: architecture is ready for controlled AI experimentation; no unrestricted medical assistant is enabled merely because infrastructure exists.

---

# 36. Cursor Agent Rules

Every Cursor agent working on this repository must follow these rules:

1. Read this PRD, architecture docs and relevant ADRs before coding.
2. Work only on the assigned milestone/scope.
3. Do not replace established architecture casually. Propose an ADR for material changes.
4. Never hard-code medical guidance into UI code when it belongs in CMS/config.
5. Never log sensitive health payloads.
6. Never introduce a third-party SDK without documenting data collection/privacy impact.
7. Keep domain calculations deterministic and independently testable.
8. Persist Gregorian/canonical dates; convert Jalali at boundaries/presentation.
9. Treat offline behavior as a primary path, not an error case.
10. Add tests for every domain rule and regression.
11. Do not leave TODOs for security-critical behavior.
12. Do not use fake production integrations silently; clearly isolate mocks to dev/test.
13. Keep OpenAPI and implementation synchronized.
14. Run all affected tests/lints before declaring completion.
15. Update milestone handoff documentation with decisions, migrations, commands, known limitations and next steps.
16. Never claim a milestone is complete if acceptance criteria fail.

---

# 37. Initial Repository Documentation Set

Create and maintain:

```text
/README.md
/PRD.md
/ARCHITECTURE.md
/SECURITY.md
/PRIVACY_ENGINEERING.md
/MEDICAL_CONTENT_GOVERNANCE.md
/ANALYTICS_SPEC.md
/API_CONVENTIONS.md
/SYNC_SPEC.md
/TESTING_STRATEGY.md
/RELEASE_CHECKLIST.md
/CLOUD_AGENT_START_PROMPT.md
/docs/adr/...
/docs/milestones/M0.md ... M12.md
/docs/handoffs/...
```

This PRD is authoritative for product intent. ADRs are authoritative for accepted technical decisions. API contracts are authoritative for wire formats. Medical content in production is authoritative only after the defined clinical review workflow.

---

# 38. Explicit Non-Goals for Initial GA

Unless separately approved:

- medical diagnosis
- prescription or medication dosing
- contraception guarantee / “safe day” feature
- clinician marketplace/telemedicine
- public social/community feed
- unrestricted AI medical chat
- partner access to intimate data
- ad targeting using reproductive-health data
- iOS client
- wearable integrations beyond explicitly approved Health Connect work

These can be evaluated later without compromising V1 architecture.

---

# 39. Final Acceptance Statement

A release is acceptable only when a Persian-speaking user can install the app, decline account creation, privately and reliably use the full core tracker offline, move among cycle/TTC/pregnancy states without losing history, understand which information is logged fact versus estimate, access clinically governed educational content, optionally synchronize/backup data, control reminders/privacy, export/delete their information, and experience a polished RTL Android product under real production conditions.

