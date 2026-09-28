#!/usr/bin/env python3
"""M9 security checklist gate — verifies required controls and docs exist."""

from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

REQUIRED = [
    "docs/security/CHECKLIST.md",
    "docs/security/BACKUP_RESTORE_DRILL.md",
    "docs/threat-model/README.md",
    "docs/adr/0016-m9-privacy-security-hardening.md",
    "android/core/security/src/main/kotlin/dev/mahin/core/security/DefaultAppLockGateway.kt",
    "backend/src/main/resources/db/migration/V7__m9_security_hardening.sql",
    "backend/src/main/kotlin/dev/mahin/backend/privacy/AccountDeletionProcessor.kt",
    "backend/src/main/kotlin/dev/mahin/backend/security/SecurityAuditService.kt",
]


def main() -> int:
    missing = [rel for rel in REQUIRED if not (ROOT / rel).exists()]
    if missing:
        print("Security checklist FAILED — missing:")
        for path in missing:
            print(f"  - {path}")
        return 1
    print("Security checklist PASS")
    return 0


if __name__ == "__main__":
    sys.exit(main())
