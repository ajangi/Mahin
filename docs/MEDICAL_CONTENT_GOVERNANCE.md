# Medical content governance

Educational/medical copy is **not** authored by engineering agents and is **not** hard-coded into the Android binary as authoritative advice.

## Lifecycle
`reference/evidence -> draft -> medical review -> editorial review -> approved -> scheduled/published -> retired`

High-risk warning content cannot be published without medical approval. Emergency copy requires four-eyes approval.

## Required metadata
Every medically relevant record includes id, slug, locale, title, summary, body, type, life stage, gestational week if any, tags, medical risk level, source references, source dates, clinical reviewer, review timestamps, next review due, version, status, effective window.

## Media
Medical-governed illustrations require the same review path (`docs/ILLUSTRATION_SYSTEM.md`). Generated or placeholder imagery is never automatically medically authoritative. M0 ships metadata contracts only.

## Android
Learn/week content is fetched from CMS APIs. Unpublished or withdrawn content must degrade safely offline. Images are never the sole carrier of safety-critical information.
