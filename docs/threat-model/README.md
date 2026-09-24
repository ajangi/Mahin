# Threat model skeleton (M0)

This is a living document. M9 performs the hardening review. Do not treat this file as a completed security assessment.

## Assets
- On-device reproductive timeline (cycles, TTC observations, pregnancy logs)
- Free-text notes
- Account credentials / session tokens (future)
- CMS credentials and unpublished medical content
- Encryption keys (Android Keystore / server KMS)
- Backups and exports

## Adversaries
- Other apps / accessibility malware on a shared phone
- Lost/stolen device
- Network observer
- Malicious or over-privileged admin/support
- Compromised third-party SDK
- Curious analytics/crash vendor

## STRIDE snapshot
| Threat | Example | M0 control | Later |
|---|---|---|---|
| Spoofing | Fake account recovery | n/a | M5 auth |
| Tampering | Altered cycle history | unsigned local DB bootstrap only | M9 encryption + M5 sync versions |
| Repudiation | Silent admin reads | n/a | M9 audit |
| Information disclosure | Period date in logcat/notification | redactors, discreet copy, no body logs | M7/M9 |
| Denial of service | Sync flood | n/a | M5 rate limits |
| Elevation | Support user dumps pregnancies | n/a | M6 least privilege |

## Explicit out of scope for M0
Production auth, SQLCipher, biometric lock, backup, Health Connect, AI assistant.
